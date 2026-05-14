package com.ddf.boot.common.core.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>对象工具类</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2022/12/16 23:17
 */
public class ObjectUtil {

    /**
     * 检查并且获取值
     *
     * @param object 对象
     * @param supplier 供应参数
     * @param <T> 泛型类型
     * @param <R> 返回值泛型类型
     */
    public static <T, R> R checkAndGet(T object, Supplier<R> supplier) {
        if (Objects.isNull(object)) {
            return null;
        }
        return supplier.get();
    }

    /**
     * 检查并且获取值，允许设置默认值
     *
     * @param object 对象
     * @param supplier 供应参数
     * @param defaultValue 参数
     * @param <T> 泛型类型
     * @param <R> 返回值泛型类型
     */
    public static <T, R> R getOrDefault(T object, R defaultValue, Supplier<R> supplier) {
        if (Objects.isNull(object)) {
            return defaultValue;
        }
        final R r = supplier.get();
        return Objects.isNull(r) ? defaultValue : r;
    }
}
