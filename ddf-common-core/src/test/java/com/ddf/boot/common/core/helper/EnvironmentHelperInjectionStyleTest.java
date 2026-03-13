package com.ddf.boot.common.core.helper;

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
 * EnvironmentHelper 注入风格测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class EnvironmentHelperInjectionStyleTest {

    @Test
    @DisplayName("EnvironmentHelper 不再使用字段 Autowired 注入")
    void shouldNotUseFieldAutowired() {
        for (Field field : EnvironmentHelper.class.getDeclaredFields()) {
            assertFalse(field.isAnnotationPresent(Autowired.class),
                    () -> "EnvironmentHelper 仍然存在字段 @Autowired: " + field.getName());
        }
    }

    @Test
    @DisplayName("EnvironmentHelper 应提供单一参数化构造函数")
    void shouldHaveSingleParameterizedConstructor() {
        Constructor<?>[] constructors = EnvironmentHelper.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() > 0));
    }
}
