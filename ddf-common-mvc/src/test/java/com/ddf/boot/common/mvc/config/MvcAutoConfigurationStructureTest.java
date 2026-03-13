package com.ddf.boot.common.mvc.config;

import com.ddf.boot.common.mvc.exception200.CommonExceptionAdvice;
import com.ddf.boot.common.mvc.permissionscan.PermissionMenuScanner;
import com.ddf.boot.common.mvc.requestsign.RequestSignAccessFilterChain;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * MVC 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class MvcAutoConfigurationStructureTest {

    @Test
    @DisplayName("MvcAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(MvcAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("mvc 具体扩展类不再依赖 Component 注解注册")
    void shouldNotUseComponentStereotype() {
        assertNotAnnotatedWithComponent(PermissionMenuScanner.class);
        assertNotAnnotatedWithComponent(RequestSignAccessFilterChain.class);
        assertNotAnnotatedWithComponent(CommonExceptionAdvice.class);
    }

    private void assertNotAnnotatedWithComponent(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> type.getName() + " 不应继续使用 @Component");
        }
    }
}
