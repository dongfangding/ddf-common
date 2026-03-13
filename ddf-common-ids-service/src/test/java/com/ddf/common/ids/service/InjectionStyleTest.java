package com.ddf.common.ids.service;

import com.ddf.common.ids.service.config.IdsServiceAutoConfiguration;
import com.ddf.common.ids.service.util.ApplicationContextUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ids-service 模块注入风格测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class InjectionStyleTest {

    @Test
    @DisplayName("ApplicationContextUtil 不再使用字段 Autowired 注入")
    void shouldNotUseFieldAutowired() {
        for (Field field : ApplicationContextUtil.class.getDeclaredFields()) {
            assertFalse(field.isAnnotationPresent(Autowired.class),
                    () -> "ApplicationContextUtil 仍然存在字段 @Autowired: " + field.getName());
        }
    }

    @Test
    @DisplayName("IdsServiceAutoConfiguration 应提供单一参数化构造函数")
    void shouldHaveSingleParameterizedConstructor() {
        Constructor<?>[] constructors = IdsServiceAutoConfiguration.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() > 0));
    }

    @Test
    @DisplayName("IdsServiceAutoConfiguration 的 Bean 方法参数不再声明 Autowired")
    void shouldNotUseAutowiredOnBeanMethodParameters() {
        for (Method method : IdsServiceAutoConfiguration.class.getDeclaredMethods()) {
            for (Parameter parameter : method.getParameters()) {
                assertFalse(parameter.isAnnotationPresent(Autowired.class),
                        () -> method.getName() + " 方法参数仍然声明 @Autowired");
            }
        }
    }
}
