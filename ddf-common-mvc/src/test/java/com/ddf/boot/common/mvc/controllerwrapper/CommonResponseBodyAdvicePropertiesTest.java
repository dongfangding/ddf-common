package com.ddf.boot.common.mvc.controllerwrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CommonResponseBodyAdviceProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class CommonResponseBodyAdvicePropertiesTest {

    @Test
    @DisplayName("默认忽略返回类型列表应初始化为空集合")
    void shouldInitializeIgnoreReturnTypeWithEmptyList() {
        CommonResponseBodyAdviceProperties properties = new CommonResponseBodyAdviceProperties();

        assertNotNull(properties.getIgnoreReturnType());
        assertTrue(properties.getIgnoreReturnType().isEmpty());
    }
}
