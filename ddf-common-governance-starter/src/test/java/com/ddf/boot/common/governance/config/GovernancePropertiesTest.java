package com.ddf.boot.common.governance.config;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GovernanceProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class GovernancePropertiesTest {

    @Test
    @DisplayName("应提供治理默认配置")
    void shouldProvideDefaultGovernanceProperties() {
        GovernanceProperties properties = new GovernanceProperties();

        assertTrue(properties.getMail().isEnabled());
        assertTrue(properties.getObservability().isEnabled());
        assertTrue(properties.getObservability().getThreadPool().isEnabled());
        assertFalse(properties.getObservability().getThreadPool().isScanAll());
        assertEquals("custom.thread.pool", properties.getObservability().getThreadPool().getMetricName());
        assertEquals(List.of("*Executor", "*executor", "*Executors", "*executors", "*Pool", "*pool", "*Scheduler",
                "*scheduler"), properties.getObservability().getThreadPool().getIncludeBeanNamePatterns());
    }

    @Test
    @DisplayName("应支持覆盖线程池治理配置")
    void shouldAllowOverrideThreadPoolProperties() {
        GovernanceProperties properties = new GovernanceProperties();

        properties.getMail().setEnabled(false);
        properties.getObservability().setEnabled(false);
        properties.getObservability().getThreadPool().setEnabled(false);
        properties.getObservability().getThreadPool().setScanAll(true);
        properties.getObservability().getThreadPool().setMetricName("biz.executor");
        properties.getObservability().getThreadPool().setIncludeBeanNamePatterns(List.of("biz*"));
        properties.getObservability().getThreadPool().setExcludeBeanNamePatterns(List.of("bizIgnore*"));

        assertFalse(properties.getMail().isEnabled());
        assertFalse(properties.getObservability().isEnabled());
        assertFalse(properties.getObservability().getThreadPool().isEnabled());
        assertTrue(properties.getObservability().getThreadPool().isScanAll());
        assertEquals("biz.executor", properties.getObservability().getThreadPool().getMetricName());
        assertEquals(List.of("biz*"), properties.getObservability().getThreadPool().getIncludeBeanNamePatterns());
        assertEquals(List.of("bizIgnore*"), properties.getObservability().getThreadPool().getExcludeBeanNamePatterns());
    }
}
