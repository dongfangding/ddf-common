package com.ddf.boot.common.limit.ratelimit.algorithm;

import com.ddf.boot.common.redis.helper.RedisTemplateHelper;

/**
 * 默认令牌桶限流算法实现。
 */
public class TokenBucketRateLimitAlgorithm implements RateLimitAlgorithm {

    public static final String ALGORITHM = "tokenBucket";

    private final RedisTemplateHelper redisTemplateHelper;

    public TokenBucketRateLimitAlgorithm(RedisTemplateHelper redisTemplateHelper) {
        this.redisTemplateHelper = redisTemplateHelper;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public boolean tryAcquire(String key, int max, int rate) {
        return redisTemplateHelper.tokenBucketRateLimitAcquire(key, max, rate);
    }
}
