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


    /**
     * 是否为主oss存储桶， 如果是的话，这个bucket会作为sts方式返回给前端， 让前端来操控这个bucket进行操作，
     * 如果需要前端上传对象， 一定要开放一个bucket给前端使用，这个属性必须为true, 注意为了实现的简单性， 只允许一个为true
     */
    private boolean primary;



}
