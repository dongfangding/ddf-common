package com.ddf.common.boot.mqttclient.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * mqtt client 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class MqttClientAutoConfigurationStructureTest {

    @Test
    @DisplayName("MqttClientAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(MqttClientAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }
}
