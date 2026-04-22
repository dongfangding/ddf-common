package com.ddf.common.ons.console.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ddf.common.ons.enume.MessageModel;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * OnsConsoleUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class OnsConsoleUtilTest {

    @Test
    @DisplayName("应根据消息模式生成重试表达式")
    void shouldBuildRetryExpression() {
        assertEquals(
                "topic-listener-group",
                OnsConsoleUtil.getRetryExpression("topic", "listener", "group", MessageModel.CLUSTERING.getModel())
        );

        String expression = OnsConsoleUtil.getRetryExpression(
                "topic", "listener", "group", MessageModel.BROADCASTING.getModel());
        String expectedPrefix = "topic-listener-group-";
        assertEquals(expectedPrefix + OnsConsoleUtil.getLocalHost(), expression);
    }

    @Test
    @DisplayName("JDK17 下动态修改注解属性值会受到模块限制")
    void shouldFailToModifyAnnotationValueUnderJdk17ModuleBoundary() {
        DemoAnnotation annotation = DemoAnnotatedClass.class.getAnnotation(DemoAnnotation.class);

        assertThrows(InaccessibleObjectException.class,
                () -> OnsConsoleUtil.modifyAnnotationValue(annotation, "topic", "changed-topic"));
    }

    @Test
    @DisplayName("应按分隔规则生成缩写")
    void shouldBuildShortNameBySplit() {
        assertEquals("o-d-i", OnsConsoleUtil.getShortNameBySplit("order-detail-item", "-"));
        assertEquals("u::n::p", OnsConsoleUtil.getShortNameBySplit("user_name_profile", "_", "::"));
        assertEquals("single", OnsConsoleUtil.getShortNameBySplit("single", "-"));
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface DemoAnnotation {
        String topic();

        String group();
    }

    @DemoAnnotation(topic = "origin-topic", group = "origin-group")
    static class DemoAnnotatedClass {
    }
}
