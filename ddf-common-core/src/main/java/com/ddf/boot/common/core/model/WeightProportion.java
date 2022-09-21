package com.ddf.boot.common.core.model;

/**
 * <p>基于权重的概率判定属性类接口定义</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/09/21 16:41
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
    Double getWeight();


}
