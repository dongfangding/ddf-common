package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.model.dto.RangeSegment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 通用物理与数学计算工具类
 *
 * @author snowball
 * @since 2026/1/19 15:20
 **/
public class CalculationUtils {

    /**
     * 计算中间过程的精度位数
     */
    private static final int CALC_SCALE = 10;

    /**
     * 根据起始值和过去的时间，计算当前所处的位置数值
     *
     * @param startValue 起始数值
     * @param elapsedMillis 已过去的时间（毫秒）
     * @param unitStep 基础单位步长
     * @param segments 定义的阶梯区间列表
     * @param defaultFactor 超出所有区间后的默认系数
     * @return 当前达到的数值
     */
    public static BigDecimal calculateCurrentValue(BigDecimal startValue, long elapsedMillis, BigDecimal unitStep,
            List<RangeSegment> segments, BigDecimal defaultFactor) {
        if (elapsedMillis <= 0) {
            return startValue;
        }

        BigDecimal remainingTime = BigDecimal.valueOf(elapsedMillis);
        BigDecimal currentPos = startValue;

        // 1. 遍历区间，消耗时间并推进数值
        for (RangeSegment segment : segments) {
            // 跳过当前位置之后的区间或已覆盖的区间
            if (currentPos.compareTo(segment.getUpperBound()) >= 0) {
                continue;
            }

            // 校验：区间连贯性检查
            if (currentPos.compareTo(segment.getLowerBound()) < 0) {
                throw new IllegalArgumentException(
                        String.format("区间不连贯！当前进度: %s, 缺失区间的下限: %s", currentPos.toPlainString(),
                                segment.getLowerBound().toPlainString()));
            }

            // 计算当前位置到本区间上限的距离
            BigDecimal distanceInSegment = segment.getUpperBound().subtract(currentPos);
            // 计算本区间内的速度：单位步长 * 系数
            BigDecimal speed = unitStep.multiply(segment.getFactor());

            // 计算走完本区间所需的时间
            BigDecimal timeNeeded = distanceInSegment.divide(speed, CALC_SCALE, RoundingMode.HALF_UP);

            if (remainingTime.compareTo(timeNeeded) >= 0) {
                // 如果剩余时间足够走完该区间
                remainingTime = remainingTime.subtract(timeNeeded);
                currentPos = segment.getUpperBound();
            } else {
                // 如果时间在本区间内用尽，计算增量数值并结束
                BigDecimal distanceCovered = remainingTime.multiply(speed);
                return currentPos.add(distanceCovered);
            }
        }

        // 2. 如果走完所有区间还有剩余时间，使用默认系数继续计算
        if (remainingTime.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal finalSpeed = unitStep.multiply(defaultFactor);
            BigDecimal finalDistance = remainingTime.multiply(finalSpeed);
            currentPos = currentPos.add(finalDistance);
        }

        return currentPos;
    }

    /**
     * 分段累加计算时长
     *
     * @param startValue 参数
     * @param targetValue 参数
     * @param unitStep 参数
     * @param segments 参数
     * @param defaultFactor 参数
     */
    public static long calculateSegmentedDuration(BigDecimal startValue, BigDecimal targetValue, BigDecimal unitStep,
            List<RangeSegment> segments, BigDecimal defaultFactor) {
        if (targetValue.compareTo(startValue) <= 0) {
            return 0L;
        }

        BigDecimal totalTimeExact = BigDecimal.ZERO;
        BigDecimal currentPos = startValue;

        for (RangeSegment segment : segments) {
            if (currentPos.compareTo(segment.getLowerBound()) < 0) {
                throw new IllegalArgumentException(
                        String.format("区间不连贯！当前进度: %s, 缺失区间的下限: %s", currentPos.toPlainString(),
                                segment.getLowerBound().toPlainString()));
            }

            if (segment.getLowerBound().compareTo(segment.getUpperBound()) >= 0) {
                throw new IllegalArgumentException("区间配置错误：下限必须小于上限");
            }

            if (currentPos.compareTo(segment.getUpperBound()) >= 0) {
                continue;
            }

            BigDecimal segmentEnd = targetValue.min(segment.getUpperBound());
            BigDecimal distance = segmentEnd.subtract(currentPos);
            BigDecimal effectiveSpeed = unitStep.multiply(segment.getFactor());

            totalTimeExact = totalTimeExact.add(distance.divide(effectiveSpeed, CALC_SCALE, RoundingMode.HALF_UP));

            currentPos = segmentEnd;

            if (currentPos.compareTo(targetValue) >= 0) {
                break;
            }
        }

        if (currentPos.compareTo(targetValue) < 0) {
            BigDecimal remainingDistance = targetValue.subtract(currentPos);
            BigDecimal finalSpeed = unitStep.multiply(defaultFactor);
            totalTimeExact = totalTimeExact.add(remainingDistance.divide(finalSpeed, CALC_SCALE, RoundingMode.HALF_UP));
        }

        return totalTimeExact.setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
