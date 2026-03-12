package com.ddf.boot.common.api.model.common.dto;

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

    private Double weightValue;

	/**
	 * 当前记录的奖励值， 虽然通过getKey能找到原始记录，然后能找到记录上绑定的所有元数据，但某些情况下不关心多余的元数据，只关心中奖的数值，
	 * 那就可以使用这个
	 */
	private Double rewardValue;
	public static DefaultWeightProportion of(String key, Double weightValue) {
		return DefaultWeightProportion.builder()
				.key(key)
				.weightValue(weightValue)
				.build();
	}


	@Override
    public String getKey() {
        return key;
    }

    @Override
    public Double getWeightValue() {
        return weightValue;
    }

    /**
     * 预留的改变原对象权重的方法
     *
     * @param newWeight 新的权重值
     */
    @Override
    public void changeOriginWeight(Double newWeight) {
        this.weightValue = newWeight;
    }
}
