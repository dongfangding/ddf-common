package com.ddf.boot.common.core.util;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GlobalAntMatcher 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class GlobalAntMatcherTest {

    @Test
    @DisplayName("应提供可复用的 AntPathMatcher 单例")
    void shouldExposeSingletonAntPathMatcher() {
        assertNotNull(GlobalAntMatcher.getAntPathMatcher());
    }

    @Test
    @DisplayName("单个 pattern 匹配应符合 Ant 风格")
    void shouldMatchSinglePattern() {
        assertTrue(GlobalAntMatcher.match("/api/**", "/api/user/list"));
        assertFalse(GlobalAntMatcher.match("/api/**", "/admin/user/list"));
    }

    @Test
    @DisplayName("多个 pattern 匹配时空列表应返回 false")
    void shouldMatchPatternsList() {
        assertTrue(GlobalAntMatcher.match(List.of("/admin/**", "/api/**"), "/api/order/query"));
        assertFalse(GlobalAntMatcher.match(List.of("/admin/**"), "/api/order/query"));
        assertFalse(GlobalAntMatcher.match(List.of(), "/api/order/query"));
    }
}
