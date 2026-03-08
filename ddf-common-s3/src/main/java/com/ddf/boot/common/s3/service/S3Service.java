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
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * S3 兼容存储服务实现.
 *
 * <p>支持 MinIO、AWS S3、阿里云 OSS 等 S3 兼容存储服务.</p>
 *
 * @author snowball
 */
@Slf4j
public class S3Service implements S3Api {

    private final MinioClient minioClient;
    private final S3Properties s3Properties;
    public S3Service(S3Properties s3Properties) {
        this.s3Properties = s3Properties;

        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(s3Properties.getEndpoint())
                .credentials(s3Properties.getAccessKey(), s3Properties.getSecretKey())
                .region(s3Properties.getRegion());

        // 如果配置了 session token，添加到请求头（MinIO SDK 不直接支持，但可以模拟）
        if (StringUtils.isNotBlank(s3Properties.getSessionToken())) {
            // AWS S3 SDK 支持 session token，但 MinIO SDK 需要通过其他方式处理
            // 这里仅记录，实际使用时需要根据具体 SDK 调整
            log.warn("当前 MinIO SDK 版本未直接支持 session token，如需使用 AWS 临时凭证，请考虑使用 AWS SDK");
        }

        this.minioClient = builder.build();
    }

    private String getDefaultBucketName() {
        return s3Properties.getPrimaryBucketProperty().getBucketName();
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }
    /**
     * @param objectKey 参数
     * @param inputStream 参数
     * @param contentType 参数
     * @param size 参数
     */
    @Override
    public UploadResult upload(String objectKey, InputStream inputStream, String contentType, long size) {
        return upload(getDefaultBucketName(), objectKey, inputStream, contentType, size);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     * @param inputStream 参数
     * @param contentType 参数
     * @param size 参数
     */
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
    /**
     * @param objectKey 参数
     * @param file 参数
     */
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
    /**
     * @param objectKey 参数
     * @param data 参数
     * @param contentType 参数
     */
    @Override
    public UploadResult upload(String objectKey, byte[] data, String contentType) {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            return upload(objectKey, inputStream, contentType, data.length);
        } catch (IOException e) {
            log.error("上传文件失败, objectKey: {}", objectKey, e);
            throw new RuntimeException("上传文件失败", e);
        }
    }
    /**
     * @param objectKey 参数
     */
    @Override
    public InputStream download(String objectKey) {
        return download(getDefaultBucketName(), objectKey);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     */
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
    /**
     * @param objectKey 参数
     */
    @Override
    public byte[] downloadAsBytes(String objectKey) {
        return downloadAsBytes(getDefaultBucketName(), objectKey);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     */
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
    /**
     * @param objectKey 参数
     */
    @Override
    public void delete(String objectKey) {
        delete(getDefaultBucketName(), objectKey);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     */
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
    /**
     * @param objectKey 参数
     */
    @Override
    public boolean exists(String objectKey) {
        return exists(getDefaultBucketName(), objectKey);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     */
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
    /**
     * @param objectKey 参数
     */
    @Override
    public String getUrl(String objectKey) {
        return getUrl(getDefaultBucketName(), objectKey);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     */
    @Override
    public String getUrl(String bucketName, String objectKey) {
        if (StringUtils.isNotBlank(s3Properties.getCustomDomain())) {
            return s3Properties.getCustomDomain() + "/" + objectKey;
        }

        if (!s3Properties.isPathStyleAccess()) {
            return s3Properties.getEndpoint() + "/" + bucketName + "/" + objectKey;
        }

        return s3Properties.getEndpoint() + "/" + bucketName + "/" + objectKey;
    }
    /**
     * @param objectKey 参数
     * @param expiry 参数
     */
    @Override
    public PresignedUrlResult getPresignedDownloadUrl(String objectKey, Duration expiry) {
        return getPresignedDownloadUrl(getDefaultBucketName(), objectKey, expiry);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     * @param expiry 参数
     */
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
    /**
     * @param objectKey 参数
     * @param contentType 参数
     * @param expiry 参数
     */
    @Override
    public PresignedUrlResult getPresignedUploadUrl(String objectKey, String contentType, Duration expiry) {
        return getPresignedUploadUrl(getDefaultBucketName(), objectKey, contentType, expiry);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     * @param contentType 参数
     * @param expiry 参数
     */
    @Override
    public PresignedUrlResult getPresignedUploadUrl(String bucketName, String objectKey, String contentType, Duration expiry) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry((int) expiry.toSeconds())
                    .method(Method.PUT)
                    .build();

            String url = minioClient.getPresignedObjectUrl(args);

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
    /**
     * @param objectKey 参数
     */
    @Override
    public S3StatObject getStat(String objectKey) {
        return getStat(getDefaultBucketName(), objectKey);
    }
    /**
     * @param bucketName 参数
     * @param objectKey 参数
     */
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
    /**
     * @param bucketName 参数
     */
    @Override
    public void makeBucket(String bucketName) {
        try {
            boolean exists = bucketExists(bucketName);
            if (!exists) {
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
    /**
     * @param bucketName 参数
     */
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
     * @param bucketName 参数
     */
    private void ensureBucketExists(String bucketName) {
        if (!bucketExists(bucketName)) {
            makeBucket(bucketName);
        }
    }
    /**
     * @param filename 参数
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

}
