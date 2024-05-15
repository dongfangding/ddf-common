package com.ddf.boot.common.sharding.config;

import java.util.Collection;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

/**
 * <p>按照后缀名分表， 直接取分表字段的值拼接到逻辑表后缀</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2023/07/20 20:02
 */
public class SuffixFieldShardingAlgorithm implements StandardShardingAlgorithm<Integer> {

    /**
     * 精准分片
     *
     * @param collection
     * @param value
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Integer> value) {
        if (collection.isEmpty()) {
            return null;
        }
        final Integer suffix = value.getValue();
        final String first = collection.stream().findFirst().get();
        // 如user_asset_gold_coin_history_202307
        final String baseTableName = first.substring(0, first.lastIndexOf("_"));
        return baseTableName + "_" + suffix;
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Integer> value) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return null;
    }
}
