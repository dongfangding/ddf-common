package com.ddf.boot.common.sharding.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Range;
import java.util.Collection;
import java.util.List;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SuffixFieldShardingAlgorithm 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class SuffixFieldShardingAlgorithmTest {

    private final SuffixFieldShardingAlgorithm algorithm = new SuffixFieldShardingAlgorithm();
    private final DataNodeInfo dataNodeInfo = new DataNodeInfo("order_", 1, '0');

    @Test
    @DisplayName("精确分片应按后缀直接拼接表名")
    void shouldShardPreciselyBySuffix() {
        String actual = algorithm.doSharding(
                List.of("order_1", "order_2"),
                new PreciseShardingValue<>("order", "user_id", dataNodeInfo, 2)
        );

        assertEquals("order_2", actual);
    }

    @Test
    @DisplayName("空集合精确分片应返回 null")
    void shouldReturnNullWhenPreciseCollectionEmpty() {
        String actual = algorithm.doSharding(
                List.of(),
                new PreciseShardingValue<>("order", "user_id", dataNodeInfo, 2)
        );

        assertNull(actual);
    }

    @Test
    @DisplayName("范围分片应返回命中的目标表")
    void shouldReturnMatchedTargetTableForRangeSharding() {
        Collection<String> actual = algorithm.doSharding(
                List.of("order_1", "order_2", "order_3"),
                new RangeShardingValue<>("order", "user_id", dataNodeInfo, Range.closed(2, 5))
        );

        assertEquals(List.of("order_2"), actual.stream().toList());
    }

    @Test
    @DisplayName("范围分片未命中时应返回空集合")
    void shouldReturnEmptyCollectionWhenRangeTargetMissing() {
        Collection<String> actual = algorithm.doSharding(
                List.of("order_1", "order_2"),
                new RangeShardingValue<>("order", "user_id", dataNodeInfo, Range.closed(3, 5))
        );

        assertTrue(actual.isEmpty());
        assertEquals("SUFFIX_FIELD_SHARDING_ALGORITHM", algorithm.getType());
    }
}
