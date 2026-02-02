package com.ddf.boot.common.s3.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * 文件上传结果.
 *
 * @author snowball
 */
@Data
@Builder
public class UploadResult {

    /**
     * 对象 Key（路径）.
     */
    private String objectKey;

    /**
     * 访问 URL.
     */
    private String url;

    /**
     * 文件大小（字节）.
     */
    private long size;

    /**
     * Content-Type.
     */
    private String contentType;

    /**
     * ETag.
     */
    private String etag;

    /**
     * 上传时间.
     */
    private Instant uploadTime;

}
