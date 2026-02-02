package com.ddf.boot.common.s3.api;

import com.ddf.boot.common.s3.model.PresignedUrlResult;
import com.ddf.boot.common.s3.model.S3StatObject;
import com.ddf.boot.common.s3.model.UploadResult;
import java.io.File;
import java.io.InputStream;
import java.time.Duration;

/**
 * S3 兼容存储操作接口.
 *
 * <p>支持 MinIO、AWS S3、阿里云 OSS 等 S3 兼容存储服务.</p>
 *
 * @author snowball
 */
public interface S3Api {

    /**
     * 上传文件（使用默认 Bucket）.
     *
     * @param objectKey 对象 Key（路径）
     * @param inputStream 输入流
     * @param contentType Content-Type
     * @param size 文件大小
     * @return 上传结果
     */
    UploadResult upload(String objectKey, InputStream inputStream, String contentType, long size);

    /**
     * 上传文件（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @param inputStream 输入流
     * @param contentType Content-Type
     * @param size 文件大小
     * @return 上传结果
     */
    UploadResult upload(String bucketName, String objectKey, InputStream inputStream, String contentType, long size);

    /**
     * 上传文件（使用 File）.
     *
     * @param objectKey 对象 Key
     * @param file 文件
     * @return 上传结果
     */
    UploadResult upload(String objectKey, File file);

    /**
     * 上传文件（使用 byte 数组）.
     *
     * @param objectKey 对象 Key
     * @param data 字节数组
     * @param contentType Content-Type
     * @return 上传结果
     */
    UploadResult upload(String objectKey, byte[] data, String contentType);

    /**
     * 下载文件（获取输入流）.
     *
     * @param objectKey 对象 Key
     * @return 输入流
     */
    InputStream download(String objectKey);

    /**
     * 下载文件（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @return 输入流
     */
    InputStream download(String bucketName, String objectKey);

    /**
     * 下载文件为字节数组.
     *
     * @param objectKey 对象 Key
     * @return 字节数组
     */
    byte[] downloadAsBytes(String objectKey);

    /**
     * 下载文件为字节数组（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @return 字节数组
     */
    byte[] downloadAsBytes(String bucketName, String objectKey);

    /**
     * 删除文件.
     *
     * @param objectKey 对象 Key
     */
    void delete(String objectKey);

    /**
     * 删除文件（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     */
    void delete(String bucketName, String objectKey);

    /**
     * 判断文件是否存在.
     *
     * @param objectKey 对象 Key
     * @return 是否存在
     */
    boolean exists(String objectKey);

    /**
     * 判断文件是否存在（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @return 是否存在
     */
    boolean exists(String bucketName, String objectKey);

    /**
     * 获取文件访问 URL.
     *
     * @param objectKey 对象 Key
     * @return 访问 URL
     */
    String getUrl(String objectKey);

    /**
     * 获取文件访问 URL（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @return 访问 URL
     */
    String getUrl(String bucketName, String objectKey);

    /**
     * 生成预签名下载 URL.
     *
     * @param objectKey 对象 Key
     * @param expiry 有效期
     * @return 预签名 URL 结果
     */
    PresignedUrlResult getPresignedDownloadUrl(String objectKey, Duration expiry);

    /**
     * 生成预签名下载 URL（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @param expiry 有效期
     * @return 预签名 URL 结果
     */
    PresignedUrlResult getPresignedDownloadUrl(String bucketName, String objectKey, Duration expiry);

    /**
     * 生成预签名上传 URL.
     *
     * @param objectKey 对象 Key
     * @param contentType Content-Type
     * @param expiry 有效期
     * @return 预签名 URL 结果
     */
    PresignedUrlResult getPresignedUploadUrl(String objectKey, String contentType, Duration expiry);

    /**
     * 生成预签名上传 URL（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @param contentType Content-Type
     * @param expiry 有效期
     * @return 预签名 URL 结果
     */
    PresignedUrlResult getPresignedUploadUrl(String bucketName, String objectKey, String contentType, Duration expiry);

    /**
     * 获取文件信息.
     *
     * @param objectKey 对象 Key
     * @return 文件信息
     */
    S3StatObject getStat(String objectKey);

    /**
     * 获取文件信息（指定 Bucket）.
     *
     * @param bucketName Bucket 名称
     * @param objectKey 对象 Key
     * @return 文件信息
     */
    S3StatObject getStat(String bucketName, String objectKey);

    /**
     * 创建 Bucket（如果不存在）.
     *
     * @param bucketName Bucket 名称
     */
    void makeBucket(String bucketName);

    /**
     * 判断 Bucket 是否存在.
     *
     * @param bucketName Bucket 名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

}
