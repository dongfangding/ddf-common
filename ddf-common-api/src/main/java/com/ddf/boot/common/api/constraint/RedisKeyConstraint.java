package com.ddf.boot.common.api.constraint;

import com.ddf.boot.common.api.enums.RedisKeyTypeEnum;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.util.PatternUtil;
import java.time.Duration;
import java.util.Objects;

/**
 * <p>redis key的约束模板</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2023/02/06 14:07
 */
public interface RedisKeyConstraint {

    /**
     * 模板变量占位符
     */
    String TEMPLATE_SPLIT_CHAR = "%s";

    /**
     * 模板内容， 占位符使用%s
     *
     * @return
     */
    String getTemplate();

    /**
     * key的过期时间定义， 只是定义，用不用取决使用方。而且也无法解决动态ttl定义问题，只是一些简单的业务规则类的提前知道有过期时间的，可以定义出来。
     * 比如验证码1分钟过期，就可以定义出来，方便追溯
     *
     * @return
     */
    Duration getTtl();

    /**
     * key的类型,只是定义出来，暂时未在getKey方法中强制将这个值拼接到key中
     *
     * @return
     */
    RedisKeyTypeEnum getRedisKeyType();

    /**
     * 获取key的分片规则
     *
     * @return
     * @param <S>
     * @param <M>
     */
    <S, M> RedisShardingRule<S, M> getRedisShardingRule();

    /**
     * 获取key
     *
     * @param args
     * @return
     */
    default String getKey(String... args) {
        String template = getTemplate();
        if (PatternUtil.findChildStrCount(template, TEMPLATE_SPLIT_CHAR) != args.length) {
            throw new BusinessException(BaseErrorCallbackCode.REDIS_KEY_ARGS_NOT_MATCH_TEMPLATE);
        }
        return String.format(template, args);
    }

    /**
     * 获取分片的key， 分开两个方法，主要是用来让使用方明确自己的意图，避免混用
     *
     * @param args
     * @return
     */
    default String getShardingKey(String... args) {
        final RedisShardingRule<Object, Object> shardingRule = getRedisShardingRule();
        if (Objects.isNull(shardingRule)) {
            return getKey(args);
        }
        return String.join("_", String.format(getTemplate(), args), shardingRule.getSharding(args));
    }
}
