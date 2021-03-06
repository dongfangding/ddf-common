package com.ddf.boot.common.redis.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/01/16 15:53
 */
public interface RedisLuaScript {

    /**
     * 基于令牌桶算法的分布式限流
     */
    RedisScript<String> TOKEN_BUCKET_RATE_LIMIT = RedisScript.of(new ClassPathResource("lua/tokenBucketRateLimit.lua"), String.class);

    /**
     * 对String类型的key进行递增递减并设置剩余过期时间的原子脚本
     */
    RedisScript<String> STRING_KEY_INCREMENT_EXPIRE = RedisScript.of(
            new ClassPathResource("lua/stringIncrementExpire.lua"), String.class);

    /**
     * 对String类型的key进行递增递减并设置过期指定指定时间的原子脚本
     */
    RedisScript<String> STRING_KEY_INCREMENT_EXPIRE_AT = RedisScript.of(
            new ClassPathResource("lua/stringIncrementExpireAt.lua"), String.class);

    /**
     * 基于滑动窗口，对指定时间段的访问总数进行控制
     */
    RedisScript<String> SLIDER_WINDOW_COUNT = RedisScript.of(
            new ClassPathResource("lua/sliderWindowCount.lua"), String.class);
}
