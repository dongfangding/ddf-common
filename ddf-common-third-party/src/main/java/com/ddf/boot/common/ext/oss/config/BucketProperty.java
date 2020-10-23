package com.ddf.boot.common.ext.oss.config;

import lombok.Data;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/13 14:31
 */
@Data
public class BucketProperty {

    /**
     * 用于自动注入时多个bucketName的OSS对象通过这个来区分
     */
    private String bucketName;

    /**
     * 构建OSS对象的属性
     * @see com.aliyun.oss.OSSClientBuilder#build(java.lang.String, java.lang.String, java.lang.String)
     */
    private String bucketEndpoint;


}
