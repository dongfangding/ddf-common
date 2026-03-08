package com.ddf.boot.common.api.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * <p>反射工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/12/18 16:07
 */
public class ReflectUtils {


    /**
     * 强制写入字段的值
     *
     * @param target
     * @param fieldName
     * @param value
     * @param forceAccess
     * @throws IllegalAccessException
     */
    public static void setFiledValue(final Object target, final String fieldName, final Object value,
            final boolean forceAccess) throws IllegalAccessException {
        FieldUtils.writeField(target, fieldName, value, true);
    }
    /**
     * @param aClass 参数
     */
    public static Field[] getFields(final Class<?> aClass) {
        return FieldUtils.getAllFields(aClass);
    }

    /**
     * 获取方法
     *
     * @param aClass
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static Method getMethod(final Class<?> aClass, final String methodName, final Class<?>... parameterTypes) {
        return MethodUtils.getAccessibleMethod(aClass, methodName, parameterTypes);
    }
}