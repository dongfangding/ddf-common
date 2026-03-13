package com.ddf.boot.common.s3.config;

import com.ddf.boot.common.s3.helper.FileUploadHelper;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * S3 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class S3AutoConfigurationStructureTest {

    @Test
    @DisplayName("S3AutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(S3AutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("S3Properties 和 FileUploadHelper 不再依赖 Component 注解注册")
    void shouldNotUseComponentStereotype() {
        assertNotAnnotatedWithComponent(S3Properties.class);
        assertNotAnnotatedWithComponent(FileUploadHelper.class);
    }

    private void assertNotAnnotatedWithComponent(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> type.getName() + " 不应继续使用 @Component");
        }
    }
}
