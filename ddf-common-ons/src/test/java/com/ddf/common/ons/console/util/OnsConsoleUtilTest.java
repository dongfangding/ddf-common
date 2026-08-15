package com.ddf.common.ons.console.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ddf.common.ons.enume.MessageModel;
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
        assertEquals("topic-listener-group",
                OnsConsoleUtil.getRetryExpression("topic", "listener", "group", MessageModel.CLUSTERING.getModel()));

        String expression = OnsConsoleUtil.getRetryExpression("topic", "listener", "group",
                MessageModel.BROADCASTING.getModel());
        String expectedPrefix = "topic-listener-group-";
        assertEquals(expectedPrefix + OnsConsoleUtil.getLocalHost(), expression);
    }

    @Test
    @DisplayName("应按分隔规则生成缩写")
    void shouldBuildShortNameBySplit() {
        assertEquals("o-d-i", OnsConsoleUtil.getShortNameBySplit("order-detail-item", "-"));
        assertEquals("u::n::p", OnsConsoleUtil.getShortNameBySplit("user_name_profile", "_", "::"));
        assertEquals("single", OnsConsoleUtil.getShortNameBySplit("single", "-"));
    }
}
