package com.ddf.boot.common.sharding.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

/**
 * 按照后缀名分表， 直接取分表字段的值拼接到逻辑表后缀.
 *
 * @author snowball
 * @version 1.0
 * @since 2023/07/20 20:02
 */
@Slf4j
public class SuffixFieldShardingAlgorithm implements StandardShardingAlgorithm<Integer> {
    @Override
    public String doSharding(final Collection<String> collection, final PreciseShardingValue<Integer> value) {
        if (collection.isEmpty()) {
            return null;
        }
        final Integer suffix = value.getValue();
        if (suffix == null) {
            return null;
        }
        final String first = collection.stream().findFirst().get();
        final int splitIndex = first.lastIndexOf("_");
        if (splitIndex < 0) {
            return null;
        }
        final String baseTableName = first.substring(0, splitIndex);
        return baseTableName + "_" + suffix;
    }

    /**
     * @param collection 参数
     * @param value 参数值
     */
    @Override
    public Collection<String> doSharding(final Collection<String> collection, final RangeShardingValue<Integer> value) {
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }

        final String first = collection.iterator().next();
        final int splitIndex = first.lastIndexOf("_");
        if (splitIndex < 0) {
            return Collections.emptyList();
        }
        final String baseTableName = first.substring(0, splitIndex);

        final Integer suffix = value.getValueRange().lowerEndpoint();
        if (suffix == null) {
            return Collections.emptyList();
        }

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

    /**
     * @param properties 参数
     */
    @Override
    public void init(final Properties properties) {
    }
}
