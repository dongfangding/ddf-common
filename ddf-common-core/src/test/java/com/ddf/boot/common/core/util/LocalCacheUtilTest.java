package com.ddf.boot.common.core.util;

import cn.hutool.cache.impl.TimedCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * LocalCacheUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
public class LocalCacheUtilTest {

    @Test
    @DisplayName("测试 getTimedCache 应创建 Hutool 定时缓存")
    public void testGetTimedCache() {
        TimedCache<String, String> cache = LocalCacheUtil.getTimedCache(1000L, 100L);

        Assertions.assertNotNull(cache);
        cache.put("key1", "value1");
        Assertions.assertEquals("value1", cache.get("key1"));
    }

    @Test
    @DisplayName("测试 getTimedCache 应在超时后清理数据")
    public void testGetTimedCacheExpire() throws InterruptedException {
        TimedCache<String, String> cache = LocalCacheUtil.getTimedCache(300L, 100L);

        cache.put("key1", "value1");
        Thread.sleep(400L);
        cache.prune();

        Assertions.assertNull(cache.get("key1"));
    }

    @Test
    @DisplayName("测试 getGuavaCache 应读取已有缓存")
    public void testGetGuavaCache() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + key;
                    }
                });
        loadingCache.put("key1", "value1");

        String result = LocalCacheUtil.getGuavaCache(loadingCache, "{0}", "key1");

        Assertions.assertEquals("value1", result);
    }

    @Test
    @DisplayName("测试 getGuavaCache 应在缺失时触发加载")
    public void testGetGuavaCacheLoadMissing() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + key;
                    }
                });

        String result = LocalCacheUtil.getGuavaCache(loadingCache, "{0}", "missingKey");

        Assertions.assertEquals("loaded_missingKey", result);
    }

    @Test
    @DisplayName("测试 getGuavaCache 异常时应返回 null")
    public void testGetGuavaCacheException() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        throw new RuntimeException("Test exception");
                    }
                });

        String result = LocalCacheUtil.getGuavaCache(loadingCache, "{0}", "errorKey");

        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("测试 getGuavaCacheCheckDefault 应将默认无效值转为 null 并清理 key")
    public void testGetGuavaCacheCheckDefaultInvalidValue() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "INVALID_DEFAULT";
                    }
                });

        String result = LocalCacheUtil.getGuavaCacheCheckDefault(loadingCache, "INVALID_DEFAULT", "{0}", "key1");

        Assertions.assertNull(result);
        Assertions.assertFalse(loadingCache.asMap().containsKey("key1"));
    }

    @Test
    @DisplayName("测试 getGuavaCacheCheckDefault 正常值应直接返回")
    public void testGetGuavaCacheCheckDefaultNormalValue() {
        LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "loaded_" + key;
                    }
                });
        loadingCache.put("key1", "normalValue");

        String result = LocalCacheUtil.getGuavaCacheCheckDefault(loadingCache, "INVALID_DEFAULT", "{0}", "key1");

        Assertions.assertEquals("normalValue", result);
    }

    @Test
    @DisplayName("测试 buildCaffeine 应在函数返回 null 时落到默认值")
    public void testBuildCaffeineDefaultValue() {
        com.github.benmanes.caffeine.cache.LoadingCache<String, String> cache = LocalCacheUtil.buildCaffeine(
                10,
                Duration.ofMinutes(1),
                key -> null,
                "DEFAULT"
        );

        Assertions.assertEquals("DEFAULT", cache.get("missing"));
    }

    @Test
    @DisplayName("测试 batchLoad 应为缺失项补默认值")
    public void testBatchLoadDefaultValueForMissingKey() {
        Map<String, String> result = LocalCacheUtil.batchLoad(
                java.util.Set.of("a", "b"),
                keys -> Map.of("a", "value-a"),
                "DEFAULT"
        );

        Assertions.assertEquals("value-a", result.get("a"));
        Assertions.assertEquals("DEFAULT", result.get("b"));
    }

    @Test
    @DisplayName("测试 getCaffeineCacheCheckDefault 单值场景应过滤空 Optional")
    public void testGetCaffeineCacheCheckDefaultOptionalValue() {
        com.github.benmanes.caffeine.cache.LoadingCache<String, Optional<String>> cache = LocalCacheUtil.buildCaffeine(
                10,
                Duration.ofMinutes(1),
                key -> Optional.empty(),
                Optional.empty()
        );

        Optional<String> result = LocalCacheUtil.getCaffeineCacheCheckDefault(cache, "missing");

        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, cache.estimatedSize());
    }

    @Test
    @DisplayName("测试 getCaffeineCacheCheckDefault 批量场景应过滤空 Optional")
    public void testGetCaffeineCacheCheckDefaultListValue() {
        com.github.benmanes.caffeine.cache.LoadingCache<String, Optional<String>> cache =
                LocalCacheUtil.buildBatchLoadCaffeine(
                        10,
                        Duration.ofMinutes(1),
                        keys -> Map.of("a", Optional.of("value-a"), "b", Optional.empty()),
                        Optional.empty()
                );

        Map<String, String> result = LocalCacheUtil.getCaffeineCacheCheckDefault(cache, List.of("a", "b"));

        Assertions.assertEquals("value-a", result.get("a"));
        Assertions.assertFalse(result.containsKey("b"));
    }

    @Test
    @DisplayName("测试 Guava refreshAfterWrite 场景应可正常重新取值")
    public void testGuavaCacheRefreshAfterWrite() throws InterruptedException, ExecutionException {
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
        Thread.sleep(150L);
        String result2 = loadingCache.get("key1");

        Assertions.assertNotNull(result1);
        Assertions.assertNotNull(result2);
    }
}
