package com.ddf.boot.common.api.constraint;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>数值类型的分片规则</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2023/02/06 15:21
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class NumberShardingRule implements RedisShardingRule<Integer, Integer> {

    /**
     * 分片字段的值在变量中的第几个位置
     */
    private Integer shardingKeyInArgsIndex;
    /**
     * 分片的取模数，如分10个分片，100个分片
     */
    private Integer shardingMod;

    @Override
    public String getSharding(String... args) {
        if (shardingKeyInArgsIndex >= args.length) {
            throw new BusinessException(BaseErrorCallbackCode.REDIS_SHARDING_KEY_NOT_MATCH_ARGS);
        }
        final String arg = args[shardingKeyInArgsIndex];
        try {
            return (Integer.parseInt(arg) % shardingMod) + "";
        } catch (Exception e) {
            throw new BusinessException(BaseErrorCallbackCode.REDIS_SHARDING_KEY_NOT_MATCH_ARGS, e);
        }
    }
}
