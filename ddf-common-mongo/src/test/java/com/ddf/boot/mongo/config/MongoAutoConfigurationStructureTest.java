package com.ddf.boot.mongo.config;

import com.ddf.boot.mongo.helper.MongoTemplateHelper;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * mongo 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class MongoAutoConfigurationStructureTest {

    @Test
    @DisplayName("MongoAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(MongoAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("MongoTemplateHelper 不再依赖 @Component 扫描注册")
    void shouldNotUseComponentStereotype() {
        for (Annotation annotation : MongoTemplateHelper.class.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> MongoTemplateHelper.class.getName() + " 不应继续使用 @Component");
        }
    }
}
