package com.ddf.boot.common.api.constraint.redis;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.ServerErrorException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>基于字符最后位数字符来进行redis key的分片策略</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/02/06 15:24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class StringLastCharShardingRule implements RedisShardingRule<Integer, Integer> {

    /**
     * 分片字段的值在模板中的第几个位置
     *
     * @param args 参数
     */
    private Integer shardingKeyInArgsIndex;

    /**
     * 分片取当前分片字段的最后几个字符
     */
    private Integer shardingMod;

    @Override
    public String getSharding(Object... args) {
        if (shardingKeyInArgsIndex >= args.length) {
            throw new ServerErrorException(BaseErrorCallbackCode.REDIS_SHARDING_KEY_NOT_MATCH_ARGS);
        }
        final String arg = (String) args[shardingKeyInArgsIndex];
        if (shardingMod > arg.length()) {
            return "";
        }
        return arg.substring(arg.length() - shardingMod);
    }
}
