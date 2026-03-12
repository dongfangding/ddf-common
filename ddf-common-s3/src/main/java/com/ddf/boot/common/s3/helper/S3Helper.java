package com.ddf.boot.common.s3.helper;

import cn.hutool.core.util.IdUtil;
import com.ddf.boot.common.s3.api.S3Api;
import com.ddf.boot.common.s3.config.S3Properties;
import com.ddf.boot.common.s3.model.UploadResult;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * S3 操作辅助类.
 *
 * <p>提供常用操作封装，简化文件上传下载流程.</p>
 *
 * @author snowball
 */
public class S3Helper {

    private final S3Api s3Api;
    private final S3Properties s3Properties;
    public S3Helper(S3Api s3Api, S3Properties s3Properties) {
        this.s3Api = s3Api;
        this.s3Properties = s3Properties;
    }

    /**
     * 获取 S3 API.
     */
    public S3Api getS3Api() {
        return s3Api;
    }

    /**
     * 获取主 Bucket 名称.
     */
    public String getPrimaryBucketName() {
        return s3Properties.getPrimaryBucketProperty().getBucketName();
    }

    /**
     * 上传文件（使用默认 Bucket，自动生成路径）.
     * @param platform 参数
     * @param identity 参数
     * @param filename 参数
     * @param inputStream 参数
     * @param contentType 参数
     * @param size 参数
     */
    public UploadResult upload(String platform, String identity, String filename,
                               InputStream inputStream, String contentType, long size) {
        String objectKey = generateObjectKey(platform, identity, filename);
        return s3Api.upload(objectKey, inputStream, contentType, size);
    }

    /**
     * 上传文件（使用默认 Bucket，自动生成路径）.
     * @param platform 参数
     * @param identity 参数
     * @param file 参数
     */
    public UploadResult upload(String platform, String identity, File file) {
        String objectKey = generateObjectKey(platform, identity, file.getName());
        return s3Api.upload(objectKey, file);
    }

    /**
     * 上传文件（使用默认 Bucket，自动生成路径）.
     * @param platform 参数
     * @param identity 参数
     * @param data 待处理数据
     * @param filename 参数
     */
    public UploadResult upload(String platform, String identity, byte[] data, String filename) {
        String contentType = getContentType(filename);
        String objectKey = generateObjectKey(platform, identity, filename);
        return s3Api.upload(objectKey, data, contentType);
    }

    /**
     * 上传文件（指定 Bucket）.
     * @param bucketName 参数
     * @param platform 参数
     * @param identity 参数
     * @param filename 参数
     * @param inputStream 参数
     * @param contentType 参数
     * @param size 参数
     */
    public UploadResult upload(String bucketName, String platform, String identity, String filename,
                               InputStream inputStream, String contentType, long size) {
        String objectKey = generateObjectKey(platform, identity, filename);
        return s3Api.upload(bucketName, objectKey, inputStream, contentType, size);
    }

    /**
     * 获取文件访问 URL.
     * @param objectKey 参数
     */
    public String getObjectUrl(String objectKey) {
        return s3Api.getUrl(objectKey);
    }

    /**
     * 临时上传文件，使用完成后自动删除.
     * @param platform 参数
     * @param identity 参数
     * @param filename 参数
     * @param inputStream 参数
     * @param contentType 参数
     * @param size 参数
     * @param consumer 参数
     */
    public void uploadAndOperate(String platform, String identity, String filename,
                                  InputStream inputStream, String contentType, long size,
                                  Consumer<String> consumer) {
        try {
            UploadResult result = upload(platform, identity, filename, inputStream, contentType, size);
            consumer.accept(result.getObjectKey());
        } finally {
            String objectKey = generateObjectKey(platform, identity, filename);
            s3Api.delete(objectKey);
        }
    }

    /**
     * 生成对象 Key（路径）.
     * @param platform 参数
     * @param identity 参数
     * @param filename 参数
     */
    public static String generateObjectKey(String platform, String identity, String filename) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String format = LocalDateTime.now().format(dtf);
        String uuid = IdUtil.simpleUUID();
        String extension = getFileExtension(filename);
        return String.format("%s/%s/%s/%s%s", platform, format, identity, uuid, extension);
    }
    /**
     * @param filename 参数
     */
    private static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    /**
     * @param filename 参数
     */
    private static String getContentType(String filename) {
        if (filename == null || !filename.contains(".")) {
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