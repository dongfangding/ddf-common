package com.ddf.boot.common.redis.helper;

import com.ddf.boot.common.redis.request.LeakyBucketRateLimitRequest;
import com.ddf.boot.common.redis.request.RateLimitRequest;
import com.ddf.boot.common.redis.script.RedisRateLimitScript;
import java.util.Collections;
import java.util.Objects;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/12/11 11:05
 */
public class RedisTemplateHelper {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    public RedisTemplateHelper(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * 全局分布式限流, 基于令牌桶算法
     * <p>
     * 底层使用hash实现， 使用ttl实现单位秒内的key过期， 格式内容如下
     * <p>
     * 一个hash结构的key内部对象类两个hash key参数
     * "last_time":"1607769790054",
     * "current_token": "0"
     * <p>
     * current_token为剩余的token，
     *
     * @param request
     */
    public boolean rateLimitAcquire(RateLimitRequest request) {
        return Objects.equals(Boolean.TRUE,
                stringRedisTemplate.execute(new RedisRateLimitScript(), Collections.singletonList(request.getKey()),
                        Integer.toString(request.getMax()), Integer.toString(request.getRate()),
                        Long.toString(System.currentTimeMillis())
                )
        );
    }


    /**
     * 全局分布式限流, 这个看起来就是基于漏桶算法的
     * <p>
     * https://github.com/redisson/redisson/wiki/6.-Distributed-objects
     *
     * @param request
     * @return
     */
    public boolean leakyBucketRateLimitAcquire(LeakyBucketRateLimitRequest request) {
        RRateLimiter limiter = redissonClient.getRateLimiter(request.getKey());
        boolean result = limiter.trySetRate(RateType.OVERALL, request.getRate(), request.getRateIntervalSeconds(),
                RateIntervalUnit.SECONDS
        );
        if (!Objects.equals(Boolean.TRUE, result)) {
            return Boolean.FALSE;
        }
        return limiter.tryAcquire();
    }

}
