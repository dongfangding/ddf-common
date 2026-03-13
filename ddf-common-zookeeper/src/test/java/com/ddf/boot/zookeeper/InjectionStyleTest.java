package com.ddf.boot.zookeeper;

import com.ddf.boot.zookeeper.monitor.config.MonitorRegistryConfig;
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
 * zookeeper 模块注入风格测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class InjectionStyleTest {

    @Test
    @DisplayName("MonitorRegistryConfig 不再使用字段 Autowired 注入")
    void shouldNotUseFieldAutowired() {
        for (Field field : MonitorRegistryConfig.class.getDeclaredFields()) {
            assertFalse(field.isAnnotationPresent(Autowired.class),
                    () -> "MonitorRegistryConfig 仍然存在字段 @Autowired: " + field.getName());
        }
    }

    @Test
    @DisplayName("MonitorRegistryConfig 应提供单一参数化构造函数")
    void shouldHaveSingleParameterizedConstructor() {
        Constructor<?>[] constructors = MonitorRegistryConfig.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() > 0));
    }
}
