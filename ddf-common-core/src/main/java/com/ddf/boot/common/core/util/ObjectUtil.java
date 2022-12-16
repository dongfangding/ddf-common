package com.ddf.boot.common.core.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>对象工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/12/16 23:17
 */
public class ObjectUtil {

    /**
     * 检查并且获取值
     *
     * @param object
     * @param supplier
     * @return
     * @param <T>
     * @param <R>
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
     * @param object
     * @param supplier
     * @return
     * @param <T>
     * @param <R>
     */
    public static <T, R> R getOrDefault(T object, R defaultValue, Supplier<R> supplier) {
        if (Objects.isNull(object)) {
            return defaultValue;
        }
        final R r = supplier.get();
        return Objects.isNull(r) ? defaultValue : r;
    }
}
