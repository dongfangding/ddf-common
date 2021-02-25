package com.ddf.boot.common.limit.ratelimit.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/23 17:59
 */
public interface RedisLuaScript {

    /**
     * 基于滑动窗口，对指定时间段的访问总数进行控制， 如果是偏向流量限流使用的话，应注意时间临界点带来的流量溢出问题， 不建议直接作为限流使用， 更偏向于
     * 业务方面的单位时间逻辑次数控制
     */
    RedisScript<String> SLIDER_WINDOW_COUNT = RedisScript.of(
            new ClassPathResource("lua/sliderWindowCount.lua"), String.class);

    /**
     * 基于令牌桶的限流脚本
     */
    RedisScript<String> TOKEN_BUCKET_RATE_LIMIT = RedisScript.of(
            new ClassPathResource("lua/tokenBucketRateLimit.lua"), String.class);
}
