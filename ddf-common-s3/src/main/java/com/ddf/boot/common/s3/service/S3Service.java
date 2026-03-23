package com.ddf.boot.common.s3.service;

import com.ddf.boot.common.s3.api.S3Api;
import com.ddf.boot.common.s3.config.S3Properties;
import com.ddf.boot.common.s3.model.PresignedUrlResult;
import com.ddf.boot.common.s3.model.S3StatObject;
import com.ddf.boot.common.s3.model.UploadResult;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.credentials.Provider;
import io.minio.credentials.StaticProvider;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

/**
 * S3 兼容存储服务实现。
 *
 * <p>当前基于 MinIO SDK 适配 S3 兼容协议，保持对 MinIO、AWS S3 以及其他兼容实现的基础能力支持。</p>
 *
 * @author snowball
 */
@Slf4j
public class S3Service implements S3Api {

    private final MinioClient minioClient;
    private final S3Properties s3Properties;

    public S3Service(S3Properties s3Properties) {
        this(createMinioClient(s3Properties), s3Properties);
    }

    /**
     * 供测试场景注入 MinioClient，避免真实网络调用。
     *
     * @param minioClient Minio 客户端
     * @param s3Properties S3 配置
     */
    public S3Service(MinioClient minioClient, S3Properties s3Properties) {
        this.minioClient = minioClient;
        this.s3Properties = s3Properties;
    }

    /**
     * 根据配置构建 MinioClient，并将超时、临时凭证、地址风格等信息下沉到 SDK 层。
     *
     * @param s3Properties S3 配置
     * @return MinioClient 实例
     */
    private static MinioClient createMinioClient(S3Properties s3Properties) {
        Provider provider;
        if (StringUtils.isNotBlank(s3Properties.getSessionToken())) {
            provider = new StaticProvider(
                    s3Properties.getAccessKey(),
                    s3Properties.getSecretKey(),
                    s3Properties.getSessionToken()
            );
        } else {
            provider = new StaticProvider(s3Properties.getAccessKey(), s3Properties.getSecretKey(), null);
        }

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(s3Properties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(s3Properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(s3Properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .build();

        MinioClient client = MinioClient.builder()
                .endpoint(s3Properties.getEndpoint())
                .credentialsProvider(provider)
                .region(s3Properties.getRegion())
                .httpClient(httpClient)
                .build();

        if (!s3Properties.isPathStyleAccess()) {
            client.enableVirtualStyleEndpoint();
        }
        return client;
    }

    private String getDefaultBucketName() {
        return s3Properties.getPrimaryBucketName();
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }

    @Override
    public UploadResult upload(String objectKey, InputStream inputStream, String contentType, long size) {
        return upload(getDefaultBucketName(), objectKey, inputStream, contentType, size);
    }

    @Override
    public UploadResult upload(String bucketName, String objectKey, InputStream inputStream, String contentType, long size) {
        try {
            ensureBucketExists(bucketName);

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build();

            ObjectWriteResponse response = minioClient.putObject(putObjectArgs);
            return UploadResult.builder()
                    .objectKey(objectKey)
                    .url(getUrl(bucketName, objectKey))
                    .size(size)
                    .contentType(contentType)
                    .etag(response.etag())
                    .uploadTime(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("上传文件失败, bucketName: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    @Override
    public UploadResult upload(String objectKey, File file) {
        try (java.io.FileInputStream inputStream = new java.io.FileInputStream(file)) {
            String contentType = getContentType(file.getName());
            return upload(objectKey, inputStream, contentType, file.length());
        } catch (IOException e) {
            log.error("上传文件失败, objectKey: {}", objectKey, e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    @Override
    public UploadResult upload(String objectKey, byte[] data, String contentType) {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            return upload(objectKey, inputStream, contentType, data.length);
        } catch (IOException e) {
            log.error("上传文件失败, objectKey: {}", objectKey, e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        return download(getDefaultBucketName(), objectKey);
    }

    @Override
    public InputStream download(String bucketName, String objectKey) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();
            return minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.error("下载文件失败, bucketName: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("下载文件失败", e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String objectKey) {
        return downloadAsBytes(getDefaultBucketName(), objectKey);
    }

    @Override
    public byte[] downloadAsBytes(String bucketName, String objectKey) {
        try (InputStream inputStream = download(bucketName, objectKey);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("下载文件失败, bucketName: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("下载文件失败", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        delete(getDefaultBucketName(), objectKey);
    }

    @Override
    public void delete(String bucketName, String objectKey) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("删除文件失败, bucketName: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("删除文件失败", e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        return exists(getDefaultBucketName(), objectKey);
    }

    @Override
    public boolean exists(String bucketName, String objectKey) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();
            minioClient.statObject(statObjectArgs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUrl(String objectKey) {
        return getUrl(getDefaultBucketName(), objectKey);
    }

    @Override
    public String getUrl(String bucketName, String objectKey) {
        if (StringUtils.isNotBlank(s3Properties.getCustomDomain())) {
            return buildUrlWithBase(s3Properties.getCustomDomain(), objectKey);
        }
        if (!s3Properties.isPathStyleAccess()) {
            return buildVirtualHostStyleUrl(bucketName, objectKey);
        }
        return buildUrlWithBase(s3Properties.getEndpoint(), bucketName + "/" + objectKey);
    }

    @Override
    public PresignedUrlResult getPresignedDownloadUrl(String objectKey, Duration expiry) {
        return getPresignedDownloadUrl(getDefaultBucketName(), objectKey, expiry);
    }

    @Override
    public PresignedUrlResult getPresignedDownloadUrl(String bucketName, String objectKey, Duration expiry) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry((int) expiry.toSeconds())
                    .method(Method.GET)
                    .build();
            String url = minioClient.getPresignedObjectUrl(args);
            return PresignedUrlResult.builder()
                    .url(url)
                    .objectKey(objectKey)
                    .expiresAt(Instant.now().plus(expiry))
                    .build();
        } catch (Exception e) {
            log.error("生成预签名下载 URL 失败, bucketName: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("生成预签名下载 URL 失败", e);
        }
    }

    @Override
    public PresignedUrlResult getPresignedUploadUrl(String objectKey, String contentType, Duration expiry) {
        return getPresignedUploadUrl(getDefaultBucketName(), objectKey, contentType, expiry);
    }

    @Override
    public PresignedUrlResult getPresignedUploadUrl(String bucketName, String objectKey, String contentType, Duration expiry) {
        try {
            GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry((int) expiry.toSeconds())
                    .method(Method.PUT);
            if (StringUtils.isNotBlank(contentType)) {
                builder.extraHeaders(Map.of("Content-Type", contentType));
            }

            String url = minioClient.getPresignedObjectUrl(builder.build());
            return PresignedUrlResult.builder()
                    .url(url)
                    .objectKey(objectKey)
                    .expiresAt(Instant.now().plus(expiry))
                    .build();
        } catch (Exception e) {
            log.error("生成预签名上传 URL 失败, bucketName: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("生成预签名上传 URL 失败", e);
        }
    }

    @Override
    public S3StatObject getStat(String objectKey) {
        return getStat(getDefaultBucketName(), objectKey);
    }

    @Override
    public S3StatObject getStat(String bucketName, String objectKey) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();
            io.minio.StatObjectResponse response = minioClient.statObject(statObjectArgs);
            return S3StatObject.builder()
                    .objectKey(objectKey)
                    .size(response.size())
                    .etag(response.etag())
                    .contentType(response.contentType())
                    .lastModified(response.lastModified().toInstant().toEpochMilli())
                    .build();
        } catch (Exception e) {
            log.error("获取文件信息失败, bucketName: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("获取文件信息失败", e);
        }
    }

    @Override
    public void makeBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build();
                minioClient.makeBucket(makeBucketArgs);
                log.info("创建 Bucket 成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建 Bucket 失败: {}", bucketName, e);
            throw new RuntimeException("创建 Bucket 失败", e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();
            return minioClient.bucketExists(bucketExistsArgs);
        } catch (Exception e) {
            log.error("检查 Bucket 是否存在失败: {}", bucketName, e);
            return false;
        }
    }

    /**
     * 上传前确保 Bucket 存在，兼容当前模块的自动建桶约定。
     *
     * @param bucketName Bucket 名称
     */
    private void ensureBucketExists(String bucketName) {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
    }

    /**
     * 根据文件名推断常见内容类型，避免直接回落到二进制流。
     *
     * @param filename 文件名
     * @return Content-Type
     */
    private String getContentType(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "application/octet-stream";
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "html" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "zip" -> "application/zip";
            case "mp3" -> "audio/mpeg";
            case "mp4" -> "video/mp4";
            default -> "application/octet-stream";
        };
    }

    /**
     * 基于 endpoint 构造虚拟主机风格 URL。
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @return 访问地址
     */
    private String buildVirtualHostStyleUrl(String bucketName, String objectKey) {
        URI endpointUri = URI.create(s3Properties.getEndpoint());
        StringBuilder builder = new StringBuilder()
                .append(endpointUri.getScheme())
                .append("://")
                .append(bucketName)
                .append(".")
                .append(endpointUri.getHost());
        if (endpointUri.getPort() > 0) {
            builder.append(":").append(endpointUri.getPort());
        }
        return appendPath(builder.toString(), objectKey);
    }

    /**
     * 基于基础地址拼接路径，统一去除重复斜杠。
     *
     * @param baseUrl 基础地址
     * @param path 路径
     * @return 完整地址
     */
    private String buildUrlWithBase(String baseUrl, String path) {
        return appendPath(baseUrl, path);
    }

    /**
     * 统一处理 URL 路径拼接。
     *
     * @param baseUrl 基础地址
     * @param path 路径
     * @return 完整地址
     */
    private String appendPath(String baseUrl, String path) {
        String normalizedBaseUrl = StringUtils.removeEnd(baseUrl, "/");
        String normalizedPath = StringUtils.removeStart(path, "/");
        return normalizedBaseUrl + "/" + normalizedPath;
    }
}
