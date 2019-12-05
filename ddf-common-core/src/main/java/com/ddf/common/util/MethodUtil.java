package com.ddf.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MethodUtil {
    private static Map<String, Set<String>> methodNameCache = new ConcurrentHashMap<>();
    private static Map<String, Set<Method>> methodCache = new ConcurrentHashMap<>();
    private static Map<String, Map<String, Method>> methodSetCache = new ConcurrentHashMap<>();
    private static Map<String, Map<String, Method>> methodGetCache = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(MethodUtil.class);

    /**
     * 获取某个对象的所有方法的名称.
     *
     * @param obj
     * @return
     */
    public static Set<String> getMethodsName(Object obj) {
        Class<? extends Object> clazz = obj.getClass();
        return getMethodsName(clazz);
    }

    /**
     * 获取某个类的所有方法的名称.
     *
     * @param clazz
     * @return
     */
    public static Set<String> getMethodsName(Class<? extends Object> clazz) {
        Set<String> methodSet;
        methodSet = methodNameCache.get(clazz.getSimpleName());
        if (methodSet == null) {
            methodSet = new HashSet<>();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                methodSet.add(method.getName());
            }
            methodNameCache.put(clazz.getSimpleName(), methodSet);
        }
        return methodSet;
    }

    /**
     * 获取某个类的所有方法.
     *
     * @param clazz
     * @return
     */
    private static Set<Method> getMethods(Class<? extends Object> clazz) {
        Set<Method> methodSet;
        methodSet = methodCache.get(clazz.getSimpleName());
        if (methodSet == null) {
            methodSet = new HashSet<>();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                methodSet.add(method);
            }
            methodCache.put(clazz.getSimpleName(), methodSet);
        }
        return methodSet;
    }

    /**
     * 获取JPA类的主键的get方法.
     *
     * @param ret
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Method getPkMethod(Object ret) {
        Method getIdMethod = null;
        Class<? extends Object> clazz;
        if (ret instanceof List) {
            clazz = ((List<Object>) ret).get(0).getClass();
        } else {
            clazz = ret.getClass();
        }
        Set<Method> f = getMethods(clazz);
        for (Method m : f) {
            if (m.getAnnotation(Id.class) != null && m.getName().startsWith("get")) {
                getIdMethod = m;
                break;
            }
        }
        return getIdMethod;
    }

    /**
     * 设置某个对象的某个属性(调用该对象属性的set方法).
     *
     * @param entity     对象
     * @param fieldName  属性名字
     * @param paramClass 参数类型
     * @param paramValue 参数的值
     */
    @SuppressWarnings({"rawtypes"})
    public static void doSetMethod(Object entity, String fieldName, Class paramClass, Object paramValue) {
        Set<String> methodSet = getMethodsName(entity);
        if (methodSet.contains("set" + StringUtils.capitalize(fieldName))) {
            try {
                Method method = entity.getClass().getMethod("set" + StringUtils.capitalize(fieldName),
                        new Class[]{paramClass});
                method.invoke(entity, new Object[]{paramValue});
            } catch (Exception e) {
                logger.error("do set method " + fieldName, e);
            }
        }
    }

    /**
     * 获取某个对象的某个属性的值(调用该对象属性的get方法).
     *
     * @param entity    对象
     * @param fieldName 属性名
     * @return
     */
    public static Object doGetMethod(Object entity, String fieldName) {
        Object ret = null;
        Set<String> methodSet = getMethodsName(entity);
        if (methodSet.contains("get" + StringUtils.capitalize(fieldName))
                || methodSet.contains(StringUtils.uncapitalize(fieldName))) {
            Method method;
            try {
                method = entity.getClass().getMethod("get" + StringUtils.capitalize(fieldName));
                ret = method.invoke(entity);
            } catch (Exception e) {
                logger.error("do get method " + fieldName, e);
            }
        }
        return ret;
    }

    /**
     * 获取对象的所有set方法
     *
     * @param obj
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Method> getSetMethods(Object obj) {
        Class clazz = obj.getClass();
        Map<String, Method> methodMap = methodSetCache.get(clazz.getSimpleName());
        if (methodMap == null) {
            methodMap = new HashMap<>();
            Set<Method> methods = getMethods(clazz);
            for (Method method : methods) {
                if (method.getName().startsWith("set")) {
                    methodMap.put(StringUtils.uncapitalize(method.getName().substring(3)), method);
                }
            }
            methodSetCache.put(clazz.getSimpleName(), methodMap);
        }
        return methodMap;
    }

    /**
     * 获取对象的所有get方法
     *
     * @param obj
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Method> getGetMethods(Object obj) {
        Class clazz = obj.getClass();
        if (obj instanceof Class) {
            clazz = (Class) obj;
        }
        Map<String, Method> methodMap = methodGetCache.get(clazz.getSimpleName());
        if (methodMap == null) {
            methodMap = new HashMap<>();
            Set<Method> methods = getMethods(clazz);
            for (Method method : methods) {
                if (method.getName().startsWith("get")) {
                    methodMap.put(StringUtils.uncapitalize(method.getName().substring(3)), method);
                }
            }
            methodGetCache.put(clazz.getSimpleName(), methodMap);
        }
        return methodMap;
    }

    /**
     * 根据给定的字符数组数据在两个对象之间拷贝数据
     *
     * @param source         源对象
     * @param taget          目标对象
     * @param copyProperties 指定需要拷贝的字段数组
     * @author dongfang.ding on 2017年1月20日
     */
    public static void entityCopy(Object source, Object taget, String[] copyProperties) {
        try {
            Class<?> sourceClazz = source.getClass();
            Class<?> targetClazz = taget.getClass();
            if (copyProperties != null && copyProperties.length > 0) {
                String updaCaseField;
                for (String prop : copyProperties) {
                    Field field = sourceClazz.getDeclaredField(prop);
                    Class<?> fieldClazz = field.getType();
                    updaCaseField = prop.substring(0, 1).toUpperCase() + prop.substring(1);
                    Method getMethod = sourceClazz.getDeclaredMethod("get" + updaCaseField);
                    Object value = getMethod.invoke(source);
                    Method setMethod = targetClazz.getDeclaredMethod("set" + updaCaseField, fieldClazz);
                    if (value != null) {
                        setMethod.invoke(taget, value);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("model copy error, ", e);
        }
    }

    /**
     * 根据给定的字符数据在两个对象之间拷贝数据
     *
     * @param source         源对象
     * @param taget          目标对象
     * @param copyProperties 指定需要拷贝的字段
     * @author dongfang.ding on 2017年1月20日
     */
    public static void entityCopy(Object source, Object taget, Set<String> copyProperties) {
        if (copyProperties != null && copyProperties.size() > 0) {
            entityCopy(source, taget, copyProperties.toArray(new String[copyProperties.size()]));
        }
    }
}
