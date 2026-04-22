package com.ddf.boot.common.redis.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * RedisCommandHelper 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class RedisCommandHelperTest {

    @Test
    @DisplayName("应暴露原始 StringRedisTemplate")
    void shouldExposeOriginalRedisTemplate() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        RedisCommandHelper helper = new RedisCommandHelper(redisTemplate);

        assertSame(redisTemplate, helper.getRedisTemplate());
    }

    @Test
    @DisplayName("set/get 应委托给 ValueOperations")
    void shouldDelegateSetAndGetToValueOperations() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        RedisCommandHelper helper = new RedisCommandHelper(redisTemplate);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("demo")).thenReturn("value");

        helper.set("demo", "value");
        helper.set("demo", "value", 30L);
        String result = helper.get("demo");

        verify(valueOperations).set("demo", "value");
        verify(valueOperations).set("demo", "value", 30L, TimeUnit.SECONDS);
        verify(valueOperations).get("demo");
        assertEquals("value", result);
    }

    @Test
    @DisplayName("过期与类型操作应委托给 RedisTemplate")
    void shouldDelegateExpireAndTypeOperations() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        RedisCommandHelper helper = new RedisCommandHelper(redisTemplate);

        when(redisTemplate.expire("demo", 60L, TimeUnit.SECONDS)).thenReturn(true);
        when(redisTemplate.type("demo")).thenReturn(DataType.STRING);

        assertEquals(Boolean.TRUE, helper.expire("demo", 60L));
        assertEquals(DataType.STRING, helper.type("demo"));
        verify(redisTemplate).expire("demo", 60L, TimeUnit.SECONDS);
        verify(redisTemplate).type("demo");
    }
}
