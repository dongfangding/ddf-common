package com.ddf.boot.common.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConstUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class ConstUtilTest {

    @Test
    @DisplayName("常量值应符合预期")
    void shouldExposeExpectedConstants() {
        assertEquals("0", ConstUtil.FALSE_STR);
        assertEquals("1", ConstUtil.TRUE_STR);
        assertEquals(Byte.valueOf("0"), ConstUtil.FALSE_BYTE);
        assertEquals(Byte.valueOf("1"), ConstUtil.TRUE_BYTE);
        assertEquals(":", ConstUtil.STRING_COLON);
        assertEquals(".", ConstUtil.STRING_DOT);
        assertEquals("%", ConstUtil.STRING_PERCENT);
    }

    @Test
    @DisplayName("isBlank 与 isNotBlank 应正确判断对象")
    void shouldCheckBlankValuesCorrectly() {
        assertTrue(ConstUtil.isBlank(null));
        assertTrue(ConstUtil.isBlank(" "));
        assertFalse(ConstUtil.isBlank("abc"));

        assertFalse(ConstUtil.isNotBlank(null));
        assertFalse(ConstUtil.isNotBlank(" "));
        assertTrue(ConstUtil.isNotBlank(123));
        assertTrue(ConstUtil.isNotBlank("abc"));
    }
}
