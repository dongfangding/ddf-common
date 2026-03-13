package com.ddf.boot.common.limit;

import com.ddf.boot.common.limit.repeatable.validator.RedisRepeatableValidator;
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
 * limit 模块注入风格测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class InjectionStyleTest {

    @Test
    @DisplayName("RedisRepeatableValidator 不再使用字段 Autowired 注入")
    void shouldNotUseFieldAutowired() {
        for (Field field : RedisRepeatableValidator.class.getDeclaredFields()) {
            assertFalse(field.isAnnotationPresent(Autowired.class),
                    () -> "RedisRepeatableValidator 仍然存在字段 @Autowired: " + field.getName());
        }
    }

    @Test
    @DisplayName("RedisRepeatableValidator 应提供单一参数化构造函数")
    void shouldHaveSingleParameterizedConstructor() {
        Constructor<?>[] constructors = RedisRepeatableValidator.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() > 0));
    }
}
