package com.ddf.boot.common.s3.model;

import lombok.Builder;
import lombok.Data;

/**
 * S3 文件信息对象.
 *
 * @author snowball
 */
@Data
@Builder
public class S3StatObject {

    /**
     * 对象 Key.
     */
    private String objectKey;

    /**
     * 文件大小（字节）.
     */
    private long size;

    /**
     * ETag.
     */
    private String etag;

    /**
     * Content-Type.
     */
    private String contentType;

    /**
     * 最后修改时间（时间戳）.
     */
    private long lastModified;

}
