package com.ddf.common.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * 反射工具类
 */
public class ReflectUtil {

    /**
     * 设置字段属性
     * @param obj
     * @param fieldName
     * @param val
     * @return
     */
    public static void invokeField(Object obj, String fieldName, Object val) {
        Field field = getField(obj, fieldName);
        if (ObjectUtil.isNull(field)) {
            throw new RuntimeException("字段不存在");
        }
        field.setAccessible(true);
        try {
            field.set(obj, val);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取字段属性
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getFieldVal(Object obj, String fieldName) {
        Field field = getField(obj, fieldName);
        if (ObjectUtil.isNull(field)) {
            throw new RuntimeException("字段不存在");
        }
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对象字段
     * @param obj
     * @param fieldName
     * @return
     */
    public static Field getField(Object obj, String fieldName) {
        if (ObjectUtil.isNull(obj) || StringUtils.isBlank(fieldName)) {
            return null;
        }
        for (Class<?> cls = obj.getClass(); !cls.isAssignableFrom(Object.class); cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                if (field.getName() == fieldName.intern()) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * 类实例化
     * @param cls
     * @return
     */
    public static <T> T newInstance(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
