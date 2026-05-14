package com.ddf.boot.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ddf.boot.common.core.model.dto.RangeSegment;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CalculationUtils 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class CalculationUtilsTest {

    @Test
    @DisplayName("calculateCurrentValue 应按分段速度累计结果")
    void shouldCalculateCurrentValueBySegments() {
        List<RangeSegment> segments = List.of(RangeSegment.of(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE),
                RangeSegment.of(BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(2)));

        BigDecimal result = CalculationUtils.calculateCurrentValue(BigDecimal.ZERO, 12L, BigDecimal.ONE, segments,
                BigDecimal.valueOf(3));

        assertTrue(result.compareTo(new BigDecimal("14")) == 0);
    }

    @Test
    @DisplayName("calculateCurrentValue 在区间之后应使用默认因子")
    void shouldUseDefaultFactorAfterAllSegments() {
        List<RangeSegment> segments = List.of(RangeSegment.of(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE),
                RangeSegment.of(BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(2)));

        BigDecimal result = CalculationUtils.calculateCurrentValue(BigDecimal.ZERO, 20L, BigDecimal.ONE, segments,
                BigDecimal.valueOf(3));

        assertTrue(result.compareTo(new BigDecimal("35")) == 0);
    }

    @Test
    @DisplayName("calculateSegmentedDuration 应按分段逆向累计耗时")
    void shouldCalculateSegmentedDuration() {
        List<RangeSegment> segments = List.of(RangeSegment.of(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE),
                RangeSegment.of(BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(2)));

        long duration = CalculationUtils.calculateSegmentedDuration(BigDecimal.ZERO, BigDecimal.valueOf(15),
                BigDecimal.ONE, segments, BigDecimal.valueOf(3));

        assertEquals(13L, duration);
    }

    @Test
    @DisplayName("区间不连续时应抛出异常")
    void shouldThrowWhenSegmentsAreNotContinuous() {
        List<RangeSegment> segments = List.of(RangeSegment.of(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE));

        IllegalArgumentException currentValueException = assertThrows(IllegalArgumentException.class,
                () -> CalculationUtils.calculateCurrentValue(BigDecimal.ZERO, 5L, BigDecimal.ONE, segments,
                        BigDecimal.ONE));
        assertTrue(currentValueException.getMessage().contains("0"));
        assertTrue(currentValueException.getMessage().contains("1"));

        IllegalArgumentException durationException = assertThrows(IllegalArgumentException.class,
                () -> CalculationUtils.calculateSegmentedDuration(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE,
                        segments, BigDecimal.ONE));
        assertTrue(durationException.getMessage().contains("0"));
        assertTrue(durationException.getMessage().contains("1"));
    }

    @Test
    @DisplayName("非法区间上下限应抛出异常")
    void shouldThrowWhenSegmentRangeIsInvalid() {
        List<RangeSegment> segments = List.of(RangeSegment.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CalculationUtils.calculateSegmentedDuration(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE,
                        segments, BigDecimal.ONE));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isEmpty());
    }
}
