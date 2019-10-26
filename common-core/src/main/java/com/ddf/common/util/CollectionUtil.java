package com.ddf.common.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具类
 */
public class CollectionUtil {

    private static final Set<Object> NULL = Collections.singleton(null);

    /**
     * 判断[1,N]集合是否为空
     *
     * @param items
     * @return
     * @since 2019年07月24日
     */
    public static boolean isBlank(Collection<?>... items) {
        if (null != items && items.length > 0) {
            for (Collection<?> item : items) {
                if (null == item) {
                    return true;
                }
                item.removeAll(NULL);
                if (item.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 判断[1,N]集合是否不为空
     *
     * @param items
     * @return
     * @since 2019年07月24日
     */
    public static boolean isNotBlank(Collection<?>... items) {
        return !isBlank(items);
    }

    /**
     * 根据Name查找属性
     *
     * @param items
     * @param name
     * @return
     * @since 2019年07月24日
     */
    @Deprecated
    public static <T> List<Object> findList(Collection<?> items, String name) {
        if (isBlank(items) || StringUtils.isBlank(name)) {
            return null;
        }
        LinkedHashSet<Object> hashSet = new LinkedHashSet<>(items.size());
        for (Object obj : items) {
            hashSet.add(ReflectUtil.getFieldVal(obj, name));
        }
        return new ArrayList<>(hashSet);
    }

    /**
     * 根据Name查找属性,并转为cls类型
     *
     * @param items
     * @param name
     * @param cls
     * @return
     * @since 2019年07月24日
     */
    public static <T> List<T> findList(Collection<?> items, String name, Class<T> cls) {
        if (isBlank(items) || StringUtils.isBlank(name) || null == cls) {
            return null;
        }
        List<T> result = new ArrayList<T>();
        for (Object item : items) {
            Object val = ReflectUtil.getFieldVal(item, name);
            if (String.class.isAssignableFrom(cls)) {
                val = String.valueOf(val);
            }
            result.add((T) val);
        }
        return result;
    }

    /**
     * 根据Name查找属性,并转为cls类型
     *
     * @param items
     * @param names
     * @param cls
     * @return
     * @since 2019年07月24日
     */
    public static <T> List<T> findList(Collection<?> items, String[] names, Class<T> cls) {
        if (isBlank(items) || ObjectUtils.isEmpty(names)) {
            return null;
        }
        LinkedHashSet<T> hashSet = new LinkedHashSet<>(items.size());
        for (Object obj : items) {
            for (String name : names) {
                hashSet.add(BeanUtil.copy(ReflectUtil.getFieldVal(obj, name), cls));
            }
        }
        return new ArrayList<T>(hashSet);
    }

    /**
     * 根据Name查找属性
     *
     * @param items
     * @param names
     * @return
     * @since 2019年07月24日
     */
    public static Map<String, List<Object>> findMap(Collection<?> items, String[] names) {
        if (isBlank(items) || ObjectUtils.isEmpty(names)) {
            return null;
        }
        return new HashMap<String, List<Object>>(items.size()) {
            private static final long serialVersionUID = 1L;

            {
                for (String name : names) {
                    put(name, findList(items, name));
                }
            }
        };
    }

    /**
     * 根据Name查找属性:并转为cls类型
     *
     * @param items
     * @param names
     * @param cls
     * @return
     * @since 2019年07月24日
     */
    public static <T> Map<String, List<T>> findMap(Collection<?> items, String[] names, Class<T> cls) {
        Map<String, List<T>> result = null;
        if (null != items && !items.isEmpty() && null != names && names.length > 0) {
            result = new HashMap<String, List<T>>(items.size());
            for (String name : names) {
                result.put(name, findList(items, name, cls));
            }
        }
        return result;
    }

    /**
     * 集合切分
     *
     * @param items
     * @param size
     * @return
     * @since 2019年07月24日
     */
    public static <T> List<List<T>> subList(Collection<T> items, int size) {
        return Lists.partition(new ArrayList<>(items), size);
    }

    /**
     * 查找元素集合
     *
     * @param items
     * @param mapper
     * @return
     * @since 2019年07月24日
     */
    public static <T, R> List<R> findList(Collection<T> items, Function<? super T, ? extends R> mapper) {
        if (isBlank(items) || ObjectUtil.anyNull(mapper)) {
            return null;
        }
        LinkedHashSet<R> hashSet = new LinkedHashSet<>(items.size());
        for (T item : items) {
            R r = mapper.apply(item);
            if (null != r) {
                hashSet.add(r);
            }
        }
        return new ArrayList<>(hashSet);
    }

    /**
     * 查找元素集合
     *
     * @param items
     * @param mappers
     * @return
     * @since 2019年07月24日
     */
    public static <T, R> List<R> findList(Collection<T> items, Function<? super T, ? extends R>... mappers) {
        if (isBlank(items) || null == mappers || mappers.length == 0) {
            return null;
        }
        LinkedHashSet<R> hashSet = new LinkedHashSet<>(items.size());
        for (T item : items) {
            for (Function<? super T, ? extends R> mapper : mappers) {
                R r = mapper.apply(item);
                if (null != r) {
                    hashSet.add(r);
                }
            }
        }
        return new ArrayList<>(hashSet);
    }

    /**
     * 集合转Map
     *
     * @param items
     * @param key
     * @param val
     * @return
     * @since 2019年07月24日
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> items, Function<T, K> key, Function<T, V> val) {
        if (isBlank(items) || null == key || null == val) {
            return Collections.emptyMap();
        }
        return items.stream().collect(Collectors.toMap(key, val));
    }

}
