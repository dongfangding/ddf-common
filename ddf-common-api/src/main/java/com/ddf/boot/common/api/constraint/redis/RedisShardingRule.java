package com.ddf.boot.common.api.constraint.redis;

/**
 * <p>redis分片key规则定义</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/02/06 15:18
 */
public interface RedisShardingRule<S, M> {

    /**
     * 获取分片字段的值在参数中的变量位置， 从0开始
     */
    S getShardingKeyInArgsIndex();

    /**
     * 获取分片的模数
     */
    M getShardingMod();

    /**
     * 返回sharding分片计算的结果
     *
     * @param args key的动态入参变量
     */
    String getSharding(Object... args);


}
