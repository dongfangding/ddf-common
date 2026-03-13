package com.ddf.boot.netty.broker.config;

import com.ddf.boot.netty.broker.server.properties.BrokerProperties;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * netty broker 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class NettyBrokerAutoConfigurationStructureTest {

    @Test
    @DisplayName("NettyBrokerAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(NettyBrokerAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("BrokerProperties 不再依赖 @Component 扫描注册")
    void shouldNotUseComponentStereotype() {
        for (Annotation annotation : BrokerProperties.class.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> BrokerProperties.class.getName() + " 不应继续使用 @Component");
        }
    }
}
