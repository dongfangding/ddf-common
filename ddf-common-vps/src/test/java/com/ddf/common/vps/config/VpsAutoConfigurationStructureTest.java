package com.ddf.common.vps.config;

import com.ddf.common.vps.helper.VpsClient;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * vps 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class VpsAutoConfigurationStructureTest {

    @Test
    @DisplayName("VpsAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(VpsAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("VpsClient 不再依赖 @Component 扫描注册")
    void shouldNotUseComponentStereotype() {
        for (Annotation annotation : VpsClient.class.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> VpsClient.class.getName() + " 不应继续使用 @Component");
        }
    }
}
