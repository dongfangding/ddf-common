package com.ddf.boot.common.redis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RedissonCustomizeProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class RedissonCustomizePropertiesTest {

    @Test
    @DisplayName("应支持 codec 属性默认值与覆盖")
    void shouldSupportCodecProperty() {
        RedissonCustomizeProperties properties = new RedissonCustomizeProperties();

        assertNull(properties.getCodec());

        properties.setCodec("org.redisson.codec.JsonJacksonCodec");

        assertEquals("org.redisson.codec.JsonJacksonCodec", properties.getCodec());
    }
}
