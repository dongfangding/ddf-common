package com.ddf.boot.common.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * NumberUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class NumberUtilTest {

    @Test
    @DisplayName("subZeroAndDot 应去除尾随零和点")
    void shouldTrimTrailingZeroAndDot() {
        assertEquals("12.3", NumberUtil.subZeroAndDot("12.300"));
        assertEquals("15", NumberUtil.subZeroAndDot("15.000"));
        assertEquals("8", NumberUtil.subZeroAndDot("8"));
    }

    @Test
    @DisplayName("isNullOrZero 应识别 null 和零值")
    void shouldRecognizeNullOrZero() {
        assertTrue(NumberUtil.isNullOrZero(null));
        assertTrue(NumberUtil.isNullOrZero(0));
        assertTrue(NumberUtil.isNullOrZero(0L));
        assertFalse(NumberUtil.isNullOrZero(1));
    }

    @Test
    @DisplayName("isNumber 应识别可创建数字")
    void shouldRecognizeCreatableNumbers() {
        assertTrue(NumberUtil.isNumber("123"));
        assertTrue(NumberUtil.isNumber("-12.34"));
        assertTrue(NumberUtil.isNumber("0x10"));
        assertFalse(NumberUtil.isNumber("12a"));
        assertFalse(NumberUtil.isNumber(" "));
    }

    @Test
    @DisplayName("convertToFractional 应转成 0.x 形式")
    void shouldConvertLongToFractional() {
        assertEquals(0.0D, NumberUtil.convertToFractional(0L));
        assertEquals(0.1234567D, NumberUtil.convertToFractional(1234567L));
        assertEquals(0.9D, NumberUtil.convertToFractional(9L));
    }

    @Test
    @DisplayName("getFractionalPartAll 应提取有效小数部分")
    void shouldExtractFractionalPart() {
        assertEquals(4567L, NumberUtil.getFractionalPartAll(123.4567D));
        assertEquals(1L, NumberUtil.getFractionalPartAll(987.0001D));
        assertEquals(0L, NumberUtil.getFractionalPartAll(1.0D));
        assertEquals(0L, NumberUtil.getFractionalPartAll(0.0D));
    }
}
