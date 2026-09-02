package com.ddf.boot.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BillNoUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class BillNoUtilTest {

    @Test
    @DisplayName("应生成包含前缀、标识和随机尾号的单号")
    void shouldGenerateBillNoWithPrefixCodeAndRandomSuffix() {
        String billNo = BillNoUtil.generationBillNo("ORD", 88L);

        assertTrue(billNo.startsWith("ORD"));
        assertTrue(billNo.contains("88"));
        assertTrue(billNo.matches("^ORD\\d{15}88\\d{5}$"));
    }

    @Test
    @DisplayName("连续生成的单号通常应不同")
    void shouldGenerateDifferentBillNos() {
        String first = BillNoUtil.generationBillNo("ORD", 1L);
        String second = BillNoUtil.generationBillNo("ORD", 1L);

        assertNotEquals(first, second);
        assertEquals(first.substring(0, 3), second.substring(0, 3));
    }
}
