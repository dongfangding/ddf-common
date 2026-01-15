package com.ddf.boot.common.core.util;

import cn.hutool.cache.impl.TimedCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * LocalCacheUtil 测试类
 *
 * @author X_Agent
 * @date 2025/01/15
 */
public class LocalCacheUtilTest {

    @Test
    @DisplayName("测试 getTimedCache - 创建Hutool定时缓存")
    public void testGetTimedCache() {
        long timeout = 1000L;
        long delay = 100L;
        TimedCache<String, String> cache = LocalCacheUtil.getTimedCache(timeout, delay);

        Assertions.assertNotNull(cache);
        cache.put("key1", "value1");
        Assertions.assertEquals("value1", cache.get("key1"));
    }

    @Test
    @DisplayName("测试 getTimedCache - 过期数据被清除")
    public void testGetTimedCache_Expire() throws InterruptedException {
        long timeout = 500L;
        long delay = 100L;
        TimedCache<String, String> cache = LocalCacheUtil.getTimedCache(timeout, delay);

        cache.put("key1", "value1");
        Thread.sleep(600L);
        cache.prune();
        Assertions.assertNull(cache.get("key1"));
    }

    @Test
    @DisplayName("测试 getGuavaCache - 正常获取缓存")
    public void testGetGuavaCache() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + key;
                    }
                });

        // 直接使用缓存
        loadingCache.put("key1", "value1");

        // 使用MessageFormat格式的key
        String template = "{0}";
        String result = LocalCacheUtil.getGuavaCache(loadingCache, template, "key1");
        Assertions.assertEquals("value1", result);
    }

    @Test
    @DisplayName("测试 getGuavaCache - 获取不存在的key触发加载")
    public void testGetGuavaCache_LoadMissing() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + key;
                    }
                });

        // 使用MessageFormat格式的key
        String template = "{0}";
        String result = LocalCacheUtil.getGuavaCache(loadingCache, template, "missingKey");
        Assertions.assertEquals("loaded_missingKey", result);
    }

    @Test
    @DisplayName("测试 getGuavaCache - 异常情况下返回null")
    public void testGetGuavaCache_Exception() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        throw new RuntimeException("Test exception");
                    }
                });

        String template = "{0}";
        String result = LocalCacheUtil.getGuavaCache(loadingCache, template, "errorKey");
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("测试 getGuavaCache - 使用模板格式化key")
    public void testGetGuavaCache_WithTemplate() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + key;
                    }
                });

        loadingCache.put("user:1001", "user1001");

        // 使用带前缀的模板
        String template = "user:{0}";
        String result = LocalCacheUtil.getGuavaCache(loadingCache, template, "1001");
        Assertions.assertEquals("user1001", result);
    }

    @Test
    @DisplayName("测试 getGuavaCacheCheckDefault - 正常值")
    public void testGetGuavaCacheCheckDefault_NormalValue() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + key;
                    }
                });

        loadingCache.put("key1", "normalValue");

        String template = "{0}";
        String result = LocalCacheUtil.getGuavaCacheCheckDefault(loadingCache, "INVALID_DEFAULT", template, "key1");
        Assertions.assertEquals("normalValue", result);
    }

    @Test
    @DisplayName("测试 getGuavaCacheCheckDefault - 默认无效值")
    public void testGetGuavaCacheCheckDefault_InvalidValue() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "INVALID_DEFAULT";
                    }
                });

        String template = "{0}";
        String result = LocalCacheUtil.getGuavaCacheCheckDefault(loadingCache, "INVALID_DEFAULT", template, "key1");
        Assertions.assertNull(result);
        Assertions.assertFalse(loadingCache.asMap().containsKey("key1"));
    }

    @Test
    @DisplayName("测试 getGuavaCacheCheckDefault - Integer类型默认无效值")
    public void testGetGuavaCacheCheckDefault_IntegerDefault() {
        LoadingCache<String, Integer> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public Integer load(String key) {
                        return -1;
                    }
                });

        loadingCache.put("key1", -1);

        String template = "{0}";
        Integer result = LocalCacheUtil.getGuavaCacheCheckDefault(loadingCache, -1, template, "key1");
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("测试 getGuavaCacheCheckDefault - 异常情况返回null")
    public void testGetGuavaCacheCheckDefault_Exception() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        throw new RuntimeException("Test exception");
                    }
                });

        String template = "{0}";
        String result = LocalCacheUtil.getGuavaCacheCheckDefault(loadingCache, "INVALID_DEFAULT", template, "errorKey");
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("测试 Guava LoadingCache - 基于时间的刷新")
    public void testGuavaCache_RefreshAfterWrite() throws InterruptedException, ExecutionException {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .refreshAfterWrite(100, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + System.currentTimeMillis();
                    }
                });

        String result1 = loadingCache.get("key1");
        Assertions.assertNotNull(result1);

        Thread.sleep(150L);

        String result2 = loadingCache.get("key1");
        Assertions.assertNotNull(result2);
    }
}
