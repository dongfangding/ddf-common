package com.ddf.boot.common.core.util;

import cn.hutool.cache.impl.TimedCache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/08/14 17:42
 */
@Slf4j
public class LocalCacheUtil {

    /**
     * 查询guava缓存
     *
     * @param loadingCache
     * @param template
     * @param parameter
     * @return
     */
    public static <T> T getGuavaCache(LoadingCache<String, T> loadingCache, String template, String... parameter) {
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
     *
     * 这是由于refreshAfterWrite当某个key已经被缓存之后，如果后续这个key对应的数据库记录被删除， load方法return null 之后
     * guava由于不接受value为null内部抛出异常，导致无法对这个key进行清除，更严重的是load方法结束之后，旧值会被填充回去，导致
     * 这个key的value永远都是最后一次的无效值。
     *
     * 所以提供一个解决思路， 需要load方法在判断如果某个key从原始数据中取数据的时候发现是null的时候要放入一个默认值代表
     * 这个值是无效的， 这样可以保证用这个值替换掉那个无效的旧值。但是在用的时候需要配合如果value是null或者是这个默认值，都
     * 代表这个值是无效的，然后就不要走业务代码了，同时需要手动把这个key删除。
     *
     * 这个方法提供获取value的时候，可以传入一个默认值， 这个方法会去判断是不是默认值，如果是默认值，会转换为null，并且清除key,
     * 方便使用的地方依然只要判断value是否为null即可
     *
     * @param loadingCache
     * @param template
     * @param parameter
     * @return
     */
    public static <T> T getGuavaCacheCheckDefault(LoadingCache<String, T> loadingCache, T defaultValidValue, String template, String... parameter) {
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
     * @param timeoutMillions
     * @param delayMillions
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> TimedCache<K, V> getTimedCache(long timeoutMillions, long delayMillions) {
        TimedCache<K, V> timedCache = new TimedCache<>(timeoutMillions);
        timedCache.schedulePrune(delayMillions);
        return timedCache;
    }
}
