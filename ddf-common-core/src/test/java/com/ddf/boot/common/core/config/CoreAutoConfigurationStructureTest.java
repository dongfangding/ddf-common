package com.ddf.boot.common.core.config;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.config.CoreAutoConfiguration;
import com.ddf.boot.common.core.promise.CompletableFutureHelper;
import com.ddf.boot.common.core.promise.DeferredHelper;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Core 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class CoreAutoConfigurationStructureTest {

    @Test
    @DisplayName("CoreAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(CoreAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("core 运行时辅助类不再依赖 Component 注解注册")
    void shouldNotUseComponentStereotype() {
        assertNotAnnotatedWithComponent(EnvironmentHelper.class);
        assertNotAnnotatedWithComponent(DeferredHelper.class);
        assertNotAnnotatedWithComponent(CompletableFutureHelper.class);
    }

    private void assertNotAnnotatedWithComponent(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> type.getName() + " 不应继续使用 @Component");
        }
    }
}
