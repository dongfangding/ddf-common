package com.ddf.boot.common.core.util;

import cn.hutool.core.util.HashUtil;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.Objects;
import java.util.function.Function;

/**
 * Object工具类
 *
 * @create 2019年07月24日
 */
public class ObjectUtil {

    private static final Interner<String> INTERNERS = Interners.newWeakInterner();

    /**
     * 对象为空
     * @since 2019年07月24日
     *
     * @param obj
     * @return
     */
    public static boolean isNull(Object obj) {
        return null == obj || obj.equals(null);
    }

    /**
     * 对象非空
     * @since 2019年07月24日
     *
     * @param obj
     * @return
     */
    public static boolean notNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * 对象含空
     * @since 2019年07月24日
     *
     * @param objects
     * @return
     */
    public static boolean anyNull(Object... objects) {
        if (null == objects || objects.length == 0) {
            return true;
        }
        for (Object obj : objects) {
            if (isNull(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对象全空
     * @since 2019年07月24日
     *
     * @param objects
     * @return
     */
    public static boolean allNull(Object... objects) {
        if (null == objects || objects.length == 0) {
            return true;
        }
        for (Object obj : objects) {
            if (notNull(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 对象无空
     * @since 2019年07月24日
     *
     * @param objects
     * @return
     */
    public static boolean allNotNull(Object... objects) {
        return !anyNull(objects);
    }

    /**
     * 字段含空
     * @since 2019年07月24日
     *
     * @param t
     * @param functions
     * @return
     */
    @SafeVarargs
    public static <T, R> boolean fieldAnyNull(T t, Function<T, R>... functions) {
        if (isNull(t)) {
            return true;
        }
        if (null != functions && functions.length > 0) {
            for (Function<T, R> call : functions) {
                if (isNull(call.apply(t))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 字段全空
     * @since 2019年07月24日
     *
     * @param t
     * @param functions
     * @return
     */
    @SafeVarargs
    public static <T, R> boolean fieldAllNull(T t, Function<T, R>... functions) {
        if (isNull(t)) {
            return true;
        }
        for (Function<T, R> call : functions) {
            if (notNull(call.apply(t))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字段无空
     * @since 2019年07月24日
     *
     * @param t
     * @param functions
     * @return
     */
    @SafeVarargs
    public static <T, R> boolean fieldAllNotNull(T t, Function<T, R>... functions) {
        return !fieldAnyNull(t, functions);
    }

    /**
     * 比较对象是否相等，先比较地址，再比较值
     * @since 2019年07月24日
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Object a, Object b) {
        if (Objects.equals(a, b)) {
            return true;
        }
        if (null != a && null != b) {
            return Objects.equals(a.toString(), b.toString());
        }
        return false;
    }
    /**
     * 获取对象Hash值
     * @since 2019年07月25日
     *
     * @param obj
     * @return
     */
    public static Integer hashInt(Object obj) {
        if (isNull(obj)) {
            return null;
        }
        return HashUtil.fnvHash(JsonUtil.toByte(obj));
    }
    /**
     * 获取对象Hash值
     * @since 2019年07月25日
     *
     * @param obj
     * @return
     */
    public static String hash(Object obj) {
        return String.valueOf(hashInt(obj));
    }
    /**
     * 获取intern，用于加锁
     * @since 2019年07月25日
     *
     * @param obj
     * @return
     */
    public static String intern(Object obj) {
        if (isNull(obj)) {
            return null;
        }
        return INTERNERS.intern(hash(obj));
    }
}

