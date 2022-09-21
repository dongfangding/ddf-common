package com.ddf.boot.common.core.model;

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
}
