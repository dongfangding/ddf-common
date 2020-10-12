package com.ddf.boot.common.ext.oss.config;

import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/12 13:28
 */
@Data
public class OssProperty {

    /**
     * 用于自动注入时多个bucketName的OSS对象通过这个来区分
     */
    private String bucketName;

    /**
     * 构建OSS对象的属性
     * @see com.aliyun.oss.OSSClientBuilder#build(java.lang.String, java.lang.String, java.lang.String)
     */
    private String bucketEndpoint;

    /**
     * 构建OSS对象的属性
     * @see com.aliyun.oss.OSSClientBuilder#build(java.lang.String, java.lang.String, java.lang.String)
     */
    private String accessKeyId;

    /**
     * 构建OSS对象的属性
     * @see com.aliyun.oss.OSSClientBuilder#build(java.lang.String, java.lang.String, java.lang.String)
     */
    private String accessKeySecret;

    /**
     * 是否加密， 如果加密，则使用系统自带的AES算法进行解密
     */
    private boolean secret;
}
