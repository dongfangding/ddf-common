package com.ddf.boot.common.s3.config;

import lombok.Data;

/**
 * S3 Bucket 配置属性.
 *
 * @author snowball
 * @see com.ddf.boot.common.s3.config.S3Properties
 */
@Data
public class S3BucketProperty {

    /**
     * Bucket 名称.
     */
    private String bucketName;

    /**
     * 是否为主 Bucket，默认为 false.
     */
    private boolean primary;

    /**
     * Bucket 访问策略: private, public-read, authenticated-read.
     */
    private String accessPolicy = "private";

}
