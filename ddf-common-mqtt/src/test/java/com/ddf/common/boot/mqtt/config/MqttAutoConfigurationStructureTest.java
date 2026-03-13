package com.ddf.common.boot.mqtt.config;

import com.ddf.common.boot.mqtt.extra.impl.MqttPublishCheckerListener;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * mqtt 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class MqttAutoConfigurationStructureTest {

    @Test
    @DisplayName("MqttAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(MqttAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("MqttPublishCheckerListener 不再依赖 @Component 扫描注册")
    void shouldNotUseComponentStereotype() {
        for (Annotation annotation : MqttPublishCheckerListener.class.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> MqttPublishCheckerListener.class.getName() + " 不应继续使用 @Component");
        }
    }
}
