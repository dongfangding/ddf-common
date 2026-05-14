package com.ddf.boot.common.core.util;

import cn.hutool.cache.impl.TimedCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/08/14 17:42
 */
@Slf4j
public class LocalCacheUtil {

    /**
     * 查询guava缓存
     *
     * @param loadingCache loading缓存参数
     * @param template template参数
     * @param parameter parameter参数
     */
    public static <T> T getGuavaCache(LoadingCache<String, T> loadingCache, String template, Object... parameter) {
        try {
            return loadingCache.get(MessageFormat.format(template, parameter));
        } catch (CacheLoader.InvalidCacheLoadException e) {
            return null;
        } catch (Exception e) {
            log.error("查询缓存异常 template={},parameter={}", template, parameter);
            return null;
        }
    }


    /**
     * 查询guava缓存, 这个方法针对于refreshAfterWrite使用
     * <p>
     * 这是由于refreshAfterWrite当某个key已经被缓存之后，如果后续这个key对应的数据库记录被删除， load方法return null 之后
     * guava由于不接受value为null内部抛出异常，导致无法对这个key进行清除，更严重的是load方法结束之后，旧值会被填充回去，导致
     * 这个key的value永远都是最后一次的无效值。
     * <p>
     * 所以提供一个解决思路， 需要load方法在判断如果某个key从原始数据中取数据的时候发现是null的时候要放入一个默认值代表
     * 这个值是无效的， 这样可以保证用这个值替换掉那个无效的旧值。但是在用的时候需要配合如果value是null或者是这个默认值，都
     * 代表这个值是无效的，然后就不要走业务代码了，同时需要手动把这个key删除。
     * <p>
     * 这个方法提供获取value的时候，可以传入一个默认值， 这个方法会去判断是不是默认值，如果是默认值，会转换为null，并且清除key,
     * 方便使用的地方依然只要判断value是否为null即可
     *
     * @param loadingCache loading缓存参数
     * @param template template参数
     * @param parameter parameter参数
     * @param defaultValidValue 参数
     */
    public static <T> T getGuavaCacheCheckDefault(LoadingCache<String, T> loadingCache, T defaultValidValue,
            String template, Object... parameter) {
        try {
            String key = MessageFormat.format(template, parameter);
            T t = loadingCache.get(key);
            if (defaultValidValue instanceof String) {
                if (Objects.equals(t, defaultValidValue)) {
                    log.info("key: {}对应的值为无效值: {}, 转换为null返回并清除该key", key, t);
                    loadingCache.invalidate(key);
                    return null;
                }
            } else {
                if (t == defaultValidValue) {
                    log.info("key: {}对应的值为无效值: {}, 转换为null返回并清除该key", key, t);
                    loadingCache.invalidate(key);
                    return null;
                }
            }
            return t;
        } catch (CacheLoader.InvalidCacheLoadException e) {
            return null;
        } catch (Exception e) {
            log.error("查询缓存异常 template={},parameter={}", template, parameter);
            return null;
        }
    }

    /**
     * 创建本地ttl缓存
     *
     * @param timeoutMillions 超时millions参数
     * @param delayMillions delaymillions参数
     * @param <K> 键泛型类型
     * @param <V> 值泛型类型
     */
    public static <K, V> TimedCache<K, V> getTimedCache(long timeoutMillions, long delayMillions) {
        TimedCache<K, V> timedCache = new TimedCache<>(timeoutMillions);
        timedCache.schedulePrune(delayMillions);
        return timedCache;
    }

    /**
     * 创建caffeine批量load缓存模板
     *
     * @param timeoutDuration 超时duration参数
     * @param function function参数
     * @param defaultValue 默认对象， 和guava一样， 本地缓存使用Map都不允许value直接存null
     * @param <K> 键泛型类型
     * @param <V> 值泛型类型
     * @param maximumSize 参数
     */
    public static <K, V> com.github.benmanes.caffeine.cache.LoadingCache<K, V> buildCaffeine(int maximumSize,
            Duration timeoutDuration, Function<K, V> function, V defaultValue) {
        return Caffeine.newBuilder().maximumSize(maximumSize).expireAfterWrite(timeoutDuration).expireAfterAccess(
                timeoutDuration).refreshAfterWrite(timeoutDuration.dividedBy(2)).recordStats().build(
                new com.github.benmanes.caffeine.cache.CacheLoader<>() {
                    /**
                     * @param key 目标键
                     */
                    @Override
                    public V load(K key) throws Exception {
                        return ObjectUtils.defaultIfNull(function.apply(key), defaultValue);
                    }
                });
    }


    /**
     * 创建caffeine批量load缓存模板
     *
     * @param timeoutDuration 超时duration参数
     * @param function function参数
     * @param defaultValue 默认对象， 和guava一样， 本地缓存使用Map都不允许value直接存null
     * @param <K> 键泛型类型
     * @param <V> 值泛型类型
     * @param maximumSize 参数
     */
    public static <K, V> com.github.benmanes.caffeine.cache.LoadingCache<K, V> buildBatchLoadCaffeine(int maximumSize,
            Duration timeoutDuration, Function<? super Iterable<K>, ? extends Map<K, V>> function, V defaultValue) {
        return Caffeine.newBuilder().maximumSize(maximumSize).expireAfterWrite(timeoutDuration).expireAfterAccess(
                timeoutDuration).refreshAfterWrite(timeoutDuration.dividedBy(2)).recordStats().build(
                new com.github.benmanes.caffeine.cache.CacheLoader<>() {
                    /**
                     * @param key 目标键
                     */
                    @Override
                    public @Nullable V load(@NonNull K key) throws Exception {
                        Map<K, V> result = function.apply(Collections.singletonList(key));
                        return (Objects.isNull(result) || Objects.isNull(result.get(key))) ? defaultValue : result.get(
                                key);
                    }

                    /**
                     * @param keys 键集合
                     */
                    @Override
                    public Map<? extends K, ? extends @NonNull V> loadAll(Set<? extends K> keys) throws Exception {
                        return batchLoad(keys, function, defaultValue);
                    }
                });
    }

    /**
     * 批量load模板
     *
     * @param keys 键集合
     * @param function function参数
     * @param <K> 键泛型类型
     * @param <V> 值泛型类型
     * @param defaultValue 参数
     */
    public static <K, V> Map<@NonNull K, @NonNull V> batchLoad(Set<? extends K> keys,
            Function<? super Iterable<K>, ? extends Map<K, V>> function, V defaultValue) {
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        Iterable<K> castKeys = (Iterable<K>) keys;
        final Map<K, V> loadResult = function.apply(castKeys);

        Map<K, V> cacheMap = new HashMap<>(keys.size());
        for (K key : keys) {
            cacheMap.put(key, loadResult.getOrDefault(key, defaultValue));
        }
        return cacheMap;
    }


    /**
     * 这种只是演示，其实大概率用不到，直接get就可以，使用Optional作为默认缓存的，就直接等待过期时间到就好了，不用主动清除key
     *
     * @param loadingCache loading缓存参数
     * @param key 目标键
     * @param <K> 键泛型类型
     * @param <V> 值泛型类型
     */
    public static <K, V> Optional<V> getCaffeineCacheCheckDefault(
            com.github.benmanes.caffeine.cache.LoadingCache<K, Optional<V>> loadingCache, K key) {
        try {
            Optional<V> t = loadingCache.get(key);
            if (Objects.nonNull(t) && t.isEmpty()) {
                log.info("caffeine load key: {}对应的值为无效值: {}, 转换为null返回并清除该key", key, t);
                loadingCache.invalidate(key);
                return Optional.empty();
            }
            return t;
        } catch (Exception e) {
            log.error("caffeine load error,  key={}", key, e);
            return Optional.empty();
        }
    }

    /**
     * 这种只是演示，其实大概率用不到，直接get就可以，使用Optional作为默认缓存的，就直接等待过期时间到就好了，不用主动清除key
     * 如果传入10个key, 8个在缓存中。caffeine会自动比较差异，只在将未命中缓存的两个key触发内部的load
     * <p>
     * 因为缓存不能为空，如果先缓存了一个值，后面这个值被删除了，且又触发了过期， load的时候一个这个id查询一定是null，再去保存就会报错，同时也会造成该过期缓存永远无法删除。
     * 如果只是一味的提供默认对象去覆盖，就会造成问题，明明知道这个key是被自己删除了，还要浪费空间去缓存。
     * 所以提供一个方法，统一封装，空对象为默认对象，是默认对象的话， 强制删除。
     *
     * @param loadingCache loading缓存参数
     * @param keys 键集合
     */
    public static <K, V> Map<K, V> getCaffeineCacheCheckDefault(
            com.github.benmanes.caffeine.cache.LoadingCache<K, Optional<V>> loadingCache, List<K> keys) {
        try {
            if (keys.isEmpty()) {
                return new HashMap<>();
            }
            Map<K, V> resultMap = new HashMap<>();
            Map<K, Optional<V>> cacheMap = loadingCache.getAll(keys);
            List<K> invalidateKeys = new ArrayList<>();
            for (Map.Entry<K, Optional<V>> entry : cacheMap.entrySet()) {
                final K key = entry.getKey();
                final Optional<V> value = entry.getValue();
                if (Objects.isNull(value) || value.isEmpty()) {
                    log.info("caffeine load all, key: {}对应的值为无效值: {}, 转换为null返回并清除该key", key, value);
                    invalidateKeys.add(key);
                } else {
                    resultMap.put(key, value.get());
                }
                //                loadingCache.invalidate(invalidateKeys);
            }
            return resultMap;
        } catch (Exception e) {
            log.error("caffeine load all error,  keys={}", keys, e);
            return new HashMap<>();
        }
    }
}
