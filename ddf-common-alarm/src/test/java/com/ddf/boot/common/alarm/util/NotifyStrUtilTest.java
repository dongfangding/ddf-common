package com.ddf.boot.common.alarm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * NotifyStrUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class NotifyStrUtilTest {

    private final NotifyStrUtil notifyStrUtil = new NotifyStrUtil();

    @Test
    @DisplayName("应返回按月分表名称的基础表名")
    void shouldReturnOriginBaseTableNameFromMonthTable() {
        assertEquals("history", notifyStrUtil.getOriginBaseTableNameFromMonth("history_202406"));
        assertEquals("order_detail", notifyStrUtil.getOriginBaseTableNameFromMonth("order_detail_202501"));
    }
}
