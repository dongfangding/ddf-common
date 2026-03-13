package com.ddf.boot.zookeeper;

import com.ddf.boot.zookeeper.monitor.config.MonitorRegistryConfig;
import com.ddf.boot.zookeeper.monitor.properties.MonitorProperties;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * zookeeper 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class ZookeeperAutoConfigurationStructureTest {

    @Test
    @DisplayName("ZookeeperAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(ZookeeperAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("zookeeper 具体类不再依赖 @Component 注册")
    void shouldNotUseComponentStereotype() {
        assertNotAnnotatedWithComponent(MonitorRegistryConfig.class);
        assertNotAnnotatedWithComponent(MonitorProperties.class);
    }

    private void assertNotAnnotatedWithComponent(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> type.getName() + " 不应继续使用 @Component");
        }
    }
}
