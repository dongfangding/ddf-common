package com.ddf.boot.common.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DateUtils 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class DateUtilsTest {

    @Test
    @DisplayName("应按月格式返回当月第一天秒级时间戳")
    void shouldResolveMonthFirstSecondsFromFormatterValue() {
        long expected = LocalDate.of(2024, 1, 1)
                .atStartOfDay(ZoneOffset.ofHours(8))
                .toEpochSecond();

        assertEquals(expected, DateUtils.getMonthFirstSecondsByDayFormatter(202401));
    }

    @Test
    @DisplayName("应按天格式返回当天开始和结束秒级时间戳")
    void shouldResolveDayBoundarySeconds() {
        long expectedStart = LocalDate.of(2024, 1, 1)
                .atStartOfDay(ZoneOffset.ofHours(8))
                .toEpochSecond();
        long expectedEnd = LocalDate.of(2024, 1, 1)
                .atTime(23, 59, 59)
                .atZone(ZoneOffset.ofHours(8))
                .toEpochSecond();

        assertEquals(expectedStart, DateUtils.getDayFirstSecondsByDayFormatter(20240101));
        assertEquals(expectedEnd, DateUtils.getDayLastSecondsByDayFormatter(20240101));
    }

    @Test
    @DisplayName("应按自然日返回开始和结束时间")
    void shouldBuildStartAndEndOfDay() {
        Date date = Date.from(Instant.parse("2026-04-20T08:09:10Z"));

        Date start = DateUtils.getStartOfDay(date);
        Date end = DateUtils.getEndOfDay(date);

        assertEquals("2026-04-20 00:00:00", DateUtils.formatDate(start));
        assertTrue(DateUtils.formatDate(end).startsWith("2026-04-20 23:59:59"));
    }

    @Test
    @DisplayName("空生日应返回空星座和零年龄")
    void shouldHandleNullBirthdayGracefully() {
        assertNull(DateUtils.getZodiac(null));
        assertEquals(0, DateUtils.getAge(null));
    }

    @Test
    @DisplayName("应返回今天开始结束与剩余秒数的有效关系")
    void shouldKeepTodayBoundaryValuesConsistent() {
        long start = DateUtils.getTodayStartTimeSeconds();
        long end = DateUtils.getTodayEndTimeSeconds();
        long remain = DateUtils.getRemainingSecondsOfToday();
        long past = DateUtils.getPastSecondsOfStartToday();

        assertEquals(24 * 60 * 60 - 1, end - start);
        assertTrue(remain >= 0);
        assertTrue(past >= 0);
        assertTrue(remain + past <= 24 * 60 * 60);
    }

    @Test
    @DisplayName("应计算月首和月末日期")
    void shouldResolveMonthBoundaryDates() {
        Date date = Date.from(LocalDate.of(2024, 2, 18)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());

        Date monthFirstDate = DateUtils.getMonthFirstDate(date);
        Date monthLastDate = DateUtils.getMonthLastDate(date);

        assertNotNull(monthFirstDate);
        assertNotNull(monthLastDate);
        assertEquals("2024-02-01 00:00:00", DateUtils.formatDate(monthFirstDate));
        assertTrue(DateUtils.formatDate(monthLastDate).startsWith("2024-02-29 23:59:59"));
    }
}
