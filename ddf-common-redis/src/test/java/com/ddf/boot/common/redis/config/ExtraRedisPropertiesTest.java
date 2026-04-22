package com.ddf.boot.common.redis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ExtraRedisProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class ExtraRedisPropertiesTest {

    @Test
    @DisplayName("应支持多 redis 节点配置与默认值")
    void shouldSupportExtraRedisPropertiesDefaults() {
        ExtraRedisProperties properties = new ExtraRedisProperties();
        ExtraRedisProperties.RedisProperties redis = new ExtraRedisProperties.RedisProperties();

        assertFalse(properties.isEnable());
        assertEquals(0, redis.getDatabase());
        assertEquals("localhost", redis.getHost());
        assertEquals(6379, redis.getPort());
        assertFalse(redis.isSsl());
        assertEquals(3000, redis.getTimeout());
    }

    @Test
    @DisplayName("应支持覆盖多 redis 节点配置")
    void shouldAllowOverrideExtraRedisProperties() {
        ExtraRedisProperties properties = new ExtraRedisProperties();
        ExtraRedisProperties.RedisProperties redis = new ExtraRedisProperties.RedisProperties();
        redis.setDatabase(2);
        redis.setUrl("redis://10.0.0.1:6380");
        redis.setHost("10.0.0.1");
        redis.setUsername("user");
        redis.setPassword("pwd");
        redis.setPort(6380);
        redis.setSsl(true);
        redis.setClientName("biz-client");
        redis.setTimeout(5000);

        properties.setEnable(true);
        properties.setMap(Map.of("biz", redis));

        assertTrue(properties.isEnable());
        assertEquals(1, properties.getMap().size());
        assertEquals(2, properties.getMap().get("biz").getDatabase());
        assertEquals("redis://10.0.0.1:6380", properties.getMap().get("biz").getUrl());
        assertEquals("biz-client", properties.getMap().get("biz").getClientName());
    }
}
