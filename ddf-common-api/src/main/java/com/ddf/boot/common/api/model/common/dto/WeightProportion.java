package com.ddf.boot.common.api.model.common.dto;

/**
 * <p>基于权重的概率判定属性类接口定义</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2022/09/21 16:41
 */
public interface WeightProportion {

    /**
     * 记录的唯一标识符
     *
     * @return
     */
    String getKey();

    /**
     * 当前记录所占权重
     *
     * @return
     */
    Double getWeightValue();

    /**
     * 预留的改变原对象权重的方法
     *
     * @param newWeight 新的权重值
     */
    default void changeOriginWeight(Double newWeight) {

    }
}
