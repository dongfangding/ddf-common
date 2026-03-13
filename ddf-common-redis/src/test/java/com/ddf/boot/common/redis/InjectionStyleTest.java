package com.ddf.boot.common.redis;

import com.ddf.boot.common.redis.config.ExtraRedissonAutoConfiguration;
import com.ddf.boot.common.redis.config.RedisCustomizeAutoConfiguration;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * redis 模块注入风格测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class InjectionStyleTest {

    @Test
    @DisplayName("redis 配置类不再使用字段 Autowired 注入")
    void shouldNotUseFieldAutowired() {
        assertNoAutowiredField(RedisCustomizeAutoConfiguration.class);
        assertNoAutowiredField(ExtraRedissonAutoConfiguration.class);
    }

    @Test
    @DisplayName("redis 配置类应提供单一参数化构造函数")
    void shouldHaveSingleParameterizedConstructor() {
        assertHasSingleParameterizedConstructor(RedisCustomizeAutoConfiguration.class);
        assertHasSingleParameterizedConstructor(ExtraRedissonAutoConfiguration.class);
    }

    private void assertNoAutowiredField(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            assertFalse(field.isAnnotationPresent(Autowired.class),
                    () -> type.getName() + " 仍然存在字段 @Autowired: " + field.getName());
        }
    }

    private void assertHasSingleParameterizedConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        assertEquals(1, constructors.length, () -> type.getName() + " 应只保留一个构造函数");
        assertTrue(Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() > 0),
                () -> type.getName() + " 应提供参数化构造函数");
    }
}
