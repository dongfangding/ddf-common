package com.ddf.boot.common.authentication.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Authentication 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class AuthenticationAutoConfigurationStructureTest {

    @Test
    @DisplayName("AuthenticationAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(AuthenticationAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }
}
