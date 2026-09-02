package com.ddf.boot.common.limit.config;

import com.ddf.boot.common.limit.LimitAutoConfiguration;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LimitAutoConfiguration 结构测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class LimitAutoConfigurationStructureTest {

    @Test
    @DisplayName("LimitAutoConfiguration 应声明 AutoConfiguration")
    void shouldDeclareAutoConfigurationAnnotation() {
        assertTrue(LimitAutoConfiguration.class.isAnnotationPresent(AutoConfiguration.class));
    }

    @Test
    @DisplayName("LimitAutoConfiguration 应为公共类")
    void shouldBePublicClass() {
        assertTrue(Modifier.isPublic(LimitAutoConfiguration.class.getModifiers()));
    }
}
