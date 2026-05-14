package com.ddf.boot.common.api.enums;

/**
 * 用于封装单个值的实例
 *
 * @author yiming
 */
public interface ValueBean<T> {

    /**
     * 获取实例的描述
     */
    String getDesc();

    /**
     * 获取实例的值
     */
    T getValue();

}
