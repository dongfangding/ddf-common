package com.ddf.boot.common.ext;

import com.ddf.boot.common.ext.oss.config.OssProperties;
import com.ddf.boot.common.ext.sms.aliyun.AliYunSmsApiImpl;
import com.ddf.boot.common.ext.sms.aliyun.config.AliYunSmsProperties;
import com.ddf.boot.common.ext.sms.aliyun.helper.AliYunSmsHelper;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 第三方自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class ExtAutoConfigurationStructureTest {

    @Test
    @DisplayName("ExtAutoConfiguration 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(ExtAutoConfiguration.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("third-party 运行时类不再依赖 Component 注解注册")
    void shouldNotUseComponentStereotype() {
        assertNotAnnotatedWithComponent(OssProperties.class);
        assertNotAnnotatedWithComponent(AliYunSmsProperties.class);
        assertNotAnnotatedWithComponent(AliYunSmsHelper.class);
        assertNotAnnotatedWithComponent(AliYunSmsApiImpl.class);
    }

    private void assertNotAnnotatedWithComponent(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            assertFalse(annotation.annotationType().equals(Component.class),
                    () -> type.getName() + " 不应继续使用 @Component");
        }
    }
}
