package com.ddf.common.util;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性拷贝工具类
 *
 * @create 2019年07月24日
 */
public class BeanUtil {

    private static final Mapper mapper = newInstance();

    /**
     * 拷贝对象
     *
     * @param source
     * @return
     * @since 2019年07月24日
     */
    @SuppressWarnings("unchecked")
    public static <T> T copy(T source) {
        if (ObjectUtil.isNull(source)) {
            return null;
        }
        return (T) copy(source, source.getClass());
    }

    /**
     * 拷贝source到target
     *
     * @param source
     * @param target
     * @return
     * @since 2019年07月24日
     */
    public static void copy(Object source, Object target) {
        if (ObjectUtil.anyNull(source, target)) {
            return;
        }
        mapper.map(source, target);
    }

    /**
     * 拷贝source到target
     *
     * @param source
     * @param target
     * @return
     * @since 2019年07月24日
     */
    public static <T> T copy(Object source, Class<T> target) {
        if (ObjectUtil.anyNull(source, target)) {
            return null;
        }
        return mapper.map(source, target);
    }

    /**
     * 拷贝集合
     *
     * @param source
     * @return
     * @since 2019年07月24日
     */
    public static <T> List<T> copy(List<T> source) {
        if (CollectionUtil.isBlank(source)) {
            return null;
        }
        return new ArrayList<T>(source.size()) {

            private static final long serialVersionUID = 1L;

            {
                for (T src : source) {
                    add(copy(src));
                }
            }
        };
    }

    /**
     * 拷贝集合，指定内容类型
     *
     * @param source
     * @param target
     * @return
     * @since 2019年07月24日
     */
    public static <E> List<E> copy(List<?> source, Class<E> target) {
        if (CollectionUtil.isBlank(source)) {
            return new ArrayList<>(0);
        }
        return new ArrayList<E>(source.size()) {

            private static final long serialVersionUID = 1L;

            {
                for (Object src : source) {
                    add(copy(src, target));
                }
            }
        };
    }

    /**
     * Bean属性输出为Map
     *
     * @param obj
     * @return
     * @since 2019年07月24日
     */
    public static Map<String, Object> toMap(Object obj) {
        if (ObjectUtil.isNull(obj)) {
            return null;
        }
        Map<String, Object> retMap = new LinkedHashMap<String, Object>();
        for (Class<?> clazz = obj.getClass(); !clazz.isAssignableFrom(Object.class); ) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    try {
                        field.setAccessible(true);
                        Object val = field.get(obj);
                        if (!ObjectUtils.isEmpty(val)) {
                            retMap.put(field.getName(), val);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return retMap;
    }

    /**
     * 返回默认实例
     *
     * @param
     * @return
     * @since 2019年07月24日
     */
    public static Mapper newInstance() {
        return DozerBeanMapperBuilder.buildDefault();
    }

}
