package com.ddf.common.ons.config;

import com.ddf.common.ons.controller.OnsConsoleController;
import com.ddf.common.ons.transaction.LocalTransactionCheckerImpl;
import com.ddf.common.ons.transaction.LocalTransactionExecutorImpl;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * ONS 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class OnsAutoConfigurationStructureTest {

    @Test
    @DisplayName("OnsAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(OnsAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("ONS 具体类不再依赖 Component 注解注册")
    void shouldNotUseComponentStereotype() {
        assertNotAnnotatedWithComponent(OnsConsoleController.class);
        assertNotAnnotatedWithComponent(LocalTransactionExecutorImpl.class);
        assertNotAnnotatedWithComponent(LocalTransactionCheckerImpl.class);
    }

    private void assertNotAnnotatedWithComponent(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> type.getName() + " 不应继续使用 @Component");
        }
    }
}
