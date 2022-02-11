package com.ddf.boot.common.core.model.request;

import java.io.Serializable;

/**
 * <p>接口签名相关参数接口类
 * 未使用基础类， 让请求类继承的方式，而是通过接口
 * </p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/01/04 20:04
 */
public interface BaseSign extends Serializable {


    /**
     * 自己系统加签字段固定参数名
     */
    String SELF_SIGNATURE_FIELD = "sign";

    /**
     * 自己系统时间戳固定参数名
     */
    String SELF_TIMESTAMP_FIELD = "timestamp";

    /**
     * 获取签名摘要值
     *
     * @return
     */
    String getSign();

    /**
     * 获取请求时间戳， 用作简单重放判断
     *
     * @return
     */
    Long getTimestamp();
}
