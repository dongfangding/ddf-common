package com.ddf.boot.common.sharding.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

/**
 * <p>按照后缀名分表， 直接取分表字段的值拼接到逻辑表后缀</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/07/20 20:02
 */
@Slf4j
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
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }

        final String first = collection.iterator().next();
        final String baseTableName = first.substring(0, first.lastIndexOf("_"));

        // 不支持跨表的范围查询，依然保持和单表一样固定用一个
        final Integer suffix = value.getValueRange().lowerEndpoint();

        String targetTable = baseTableName + "_" + suffix;

        if (collection.contains(targetTable)) {
            return Collections.singletonList(targetTable);
        }
        return Collections.emptyList();
    }


    @Override
    public String getType() {
        return "SUFFIX_FIELD_SHARDING_ALGORITHM";
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }
}
