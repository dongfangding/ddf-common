package com.ddf.boot.common.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class DefaultWeightProportion implements WeightProportion {

    private String key;

    private Double weight;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Double getWeight() {
        return weight;
    }

    /**
     * 预留的改变原对象权重的方法
     *
     * @param newWeight
     */
    @Override
    public void changeOriginWeight(Double newWeight) {
        this.weight = newWeight;
    }
}
