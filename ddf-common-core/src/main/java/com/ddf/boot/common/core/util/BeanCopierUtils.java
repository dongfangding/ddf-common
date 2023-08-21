package com.ddf.boot.common.core.util;

import cn.hutool.core.collection.CollUtil;
import com.esotericsoftware.reflectasm.ConstructorAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;
import org.springframework.util.Assert;

/**
 * Bean拷贝工具类
 * 暂不支持同属性名原始类型与包装类型的拷贝
 *
 * 这个是有缓存的，第一次会与其它使用反射实现的速度差不多，但后面就非常快了，和mapstruct类似的字节码技术的速度就几乎持平了
 *
 * @author SteveGuo
 * @date 2018-08-21 10:54 PM
 */
public class BeanCopierUtils {

    private static final Map<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ConstructorAccess> CONSTRUCTOR_ACCESS_CACHE = new ConcurrentHashMap<>();

    /**
     * 单个Bean拷贝
     *
     * @param sourceInstance 源实例
     * @param targetClass    目标类
     * @param <T>            目标类型
     * @return
     */
    public static <T> T copy(Object sourceInstance, Class<T> targetClass) {
        return copy(sourceInstance, targetClass, null);
    }

    /**
     * 单个Bean拷贝
     *
     * @param sourceInstance 源实例
     * @param targetClass    目标类
     * @param <T>            目标类型
     * @param converter      同名称不同类型的转换器
     * @return
     */
    public static <T> T copy(Object sourceInstance, Class<T> targetClass, Converter converter) {
        if (Objects.isNull(sourceInstance)) {
            return null;
        }
        Assert.notNull(sourceInstance, "sourceInstance must not be null");
        Assert.notNull(targetClass, "targetClass must not be null");
        T target;
        try {
            target = targetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(
                    String.format("Create new instance of %s failed: %s", targetClass, e.getMessage()));
        }
        copy(sourceInstance, target, converter);
        return target;
    }

    /**
     * Bean列表拷贝
     *
     * @param sourceInstanceList 源实例列表
     * @param targetClass        目标类
     * @param <T>                目标类型
     * @return
     */
    public static <T> List<T> copy(List<?> sourceInstanceList, Class<T> targetClass) {
        return copy(sourceInstanceList, targetClass, null, true);
    }

    /**
     * Bean列表拷贝
     *
     * @param sourceInstanceList 源实例列表
     * @param targetClass        目标类
     * @param <T>                目标类型
     * @param needSort           是否需要排序，如果为false，则会采用并行流拷贝，不能保持原来的排序，如果为true，采用串行流拷贝，保持原来的排序
     * @return
     */
    public static <T> List<T> copy(List<?> sourceInstanceList, Class<T> targetClass, boolean needSort) {
        return copy(sourceInstanceList, targetClass, null, needSort);
    }

    /**
     * Bean列表拷贝
     *
     * @param sourceInstanceList 源实例列表
     * @param targetClass        目标类
     * @param <T>                目标类型
     * @param converter          同名称不同类型的转换器
     * @param needSort           是否需要排序，如果为false，则会采用并行流拷贝，不能保持原来的排序，如果为true，采用串行流拷贝，保持原来的排序
     * @return
     */
    public static <T> List<T> copy(List<?> sourceInstanceList, Class<T> targetClass, Converter converter,
            boolean needSort) {
        if (CollUtil.isEmpty(sourceInstanceList)) {
            return new ArrayList<>();
        }
        Assert.notNull(sourceInstanceList, "sourceInstanceList must not be null");
        Assert.notNull(targetClass, "targetClass must not be null");
        Stream<?> stream;
        if (needSort) {
            stream = sourceInstanceList.stream();
        } else {
            stream = sourceInstanceList.parallelStream();
        }
        return stream.map(obj -> {
            T target;
            try {
                target = getConstructorAccess(targetClass).newInstance();
                copy(obj, target, converter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return target;
        }).collect(Collectors.toList());
    }

    public static void copy(Object source, Object target) {
        copy(source, target, null);
    }

    public static void copy(Object source, Object target, Converter converter) {
        boolean useConverter = null != converter;
        BeanCopier copier = getBeanCopier(source.getClass(), target.getClass(), useConverter);
        copier.copy(source, target, converter);
    }

    private static BeanCopier getBeanCopier(Class sourceClass, Class targetClass, boolean useConverter) {
        String beanKey = generateKey(sourceClass, targetClass);
        BeanCopier copier;
        if (!BEAN_COPIER_CACHE.containsKey(beanKey)) {
            copier = BeanCopier.create(sourceClass, targetClass, useConverter);
            BEAN_COPIER_CACHE.put(beanKey, copier);
        } else {
            copier = BEAN_COPIER_CACHE.get(beanKey);
        }
        return copier;
    }

    private static String generateKey(Class<?> class1, Class<?> class2) {
        return class1.toString() + class2.toString();
    }

    private static <T> ConstructorAccess<T> getConstructorAccess(Class<T> targetClass) {
        ConstructorAccess<T> constructorAccess = CONSTRUCTOR_ACCESS_CACHE.get(targetClass.toString());
        if (constructorAccess != null) {
            return constructorAccess;
        }
        try {
            constructorAccess = ConstructorAccess.get(targetClass);
            constructorAccess.newInstance();
            CONSTRUCTOR_ACCESS_CACHE.put(targetClass.toString(), constructorAccess);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Create new instance of %s failed: %s", targetClass, e.getMessage()));
        }
        return constructorAccess;
    }
}
