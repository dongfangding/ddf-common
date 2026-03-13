package com.ddf.boot.common.rocketmq.config;

import com.ddf.boot.common.rocketmq.producer.RocketProducer;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * rocketmq 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class RocketMQEnhanceAutoConfigurationStructureTest {

    @Test
    @DisplayName("RocketMQEnhanceAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(RocketMQEnhanceAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("RocketProducer 不再依赖 @Component 扫描注册")
    void shouldNotUseComponentStereotype() {
        for (Annotation annotation : RocketProducer.class.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> RocketProducer.class.getName() + " 不应继续使用 @Component");
        }
    }
}
