package com.ddf.boot.common.api.constraint;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>基于字符最后位数字符来进行redis key的分片策略</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2023/02/06 15:24
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class StringLastCharShardingRule implements RedisShardingRule<Integer, Integer> {

    /**
     * 分片字段的值在模板中的第几个位置
     */
    private Integer shardingKeyInArgsIndex;

    /**
     * 分片取当前分片字段的最后几个字符
     */
    private Integer shardingMod;

    @Override
    public String getSharding(String... args) {
        if (shardingKeyInArgsIndex >= args.length) {
            throw new BusinessException(BaseErrorCallbackCode.REDIS_SHARDING_KEY_NOT_MATCH_ARGS);
        }
        final String arg = args[shardingKeyInArgsIndex];
        if (shardingMod >= arg.length()) {
            return "";
        }
        return arg.substring(arg.length() - shardingMod);
    }

    public static void main(String[] args) {
        System.out.println(StringLastCharShardingRule.of(0, 2).getSharding("hello"));
        System.out.println(StringLastCharShardingRule.of(1, 4).getSharding("hello", "world"));
        System.out.println(StringLastCharShardingRule.of(2, 2).getSharding("hello", "world", "java"));
    }
}
