package com.ddf.boot.common.alarm.config;

import com.ddf.boot.common.alarm.notify.CodeExceptionNotify;
import com.ddf.boot.common.alarm.notify.TableNotifyImpl;
import com.ddf.boot.common.alarm.rule.tablescan.TableScan;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * alarm 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class AlarmAutoConfigurationStructureTest {

    @Test
    @DisplayName("AlarmAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(AlarmAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("alarm 具体类不再依赖扫描式 stereotype 注册")
    void shouldNotUseComponentStereotype() {
        assertNotAnnotatedWithComponent(CodeExceptionNotify.class);
        assertNotAnnotatedWithComponent(TableNotifyImpl.class);
        assertNotAnnotatedWithConfiguration(TableScan.class);
    }

    private void assertNotAnnotatedWithComponent(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> type.getName() + " 不应继续使用 @Component");
        }
    }

    private void assertNotAnnotatedWithConfiguration(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Configuration.class),
                    () -> type.getName() + " 不应继续使用 @Configuration");
        }
    }
}
