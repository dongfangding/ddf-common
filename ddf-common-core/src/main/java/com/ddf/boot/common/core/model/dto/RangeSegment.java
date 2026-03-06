package com.ddf.boot.common.core.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 速度区间定义
 *
 * @author snowball
 * @date 2026/1/19 15:18
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class RangeSegment {

    /**
     * 该区间下限值，包含
     */
    private BigDecimal lowerBound;

    /**
     * 该区间上限值，包含
     */
    private BigDecimal upperBound;
    /**
     * 该区间相对于基础值的加权因子
     */
    private BigDecimal factor;
}
