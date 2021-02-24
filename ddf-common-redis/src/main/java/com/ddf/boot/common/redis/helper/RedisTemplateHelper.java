package com.ddf.boot.common.redis.helper;

import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.redis.request.LeakyBucketRateLimitRequest;
import com.ddf.boot.common.redis.request.RateLimitRequest;
import com.ddf.boot.common.redis.script.RedisLuaScript;
import java.util.Collections;
import java.util.Date;
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
        return Integer.parseInt(Objects.requireNonNull(
                stringRedisTemplate.execute(RedisLuaScript.TOKEN_BUCKET_RATE_LIMIT, Collections.singletonList(request.getKey()),
                        Integer.toString(request.getMax()), Integer.toString(request.getRate()),
                        Long.toString(System.currentTimeMillis())
                ))) > 0;
    }

    /**
     * 对String类型的key进行递增递减并设置过期值的原子脚本, 初始值为0， 每次递增+1
     *
     * @param key           key
     * @param expireSeconds 过期秒值
     * @return 缓存key对应的最新值
     */
    public Long incrementKeyExpire(String key, long expireSeconds) {
        return Long.parseLong(Objects.requireNonNull(
                stringRedisTemplate.execute(RedisLuaScript.STRING_KEY_INCREMENT_EXPIRE, Collections.singletonList(key),
                        "1", String.valueOf(expireSeconds)
                )));
    }


    /**
     * 对String类型的key进行递增递减并设置过期指定指定时间的原子脚本
     * 时间小于当前时间，会导致tt为-1, 这里不进行校验
     *
     * @param key      key
     * @param expireAt 指定过期的具体时间
     * @return 缓存key对应的最新值
     */
    public Long incrementKeyExpireAt(String key, Date expireAt) {
        return Long.parseLong(Objects.requireNonNull(
                stringRedisTemplate.execute(RedisLuaScript.STRING_KEY_INCREMENT_EXPIRE_AT,
                        Collections.singletonList(key), "1",
                        // 这个单位是秒
                        String.valueOf(expireAt.getTime() / 1000)
                )));
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


    /**
     * 控制某个时间窗口类，对访问总次数进行控制
     *
     * @param key
     * @param maxCount
     * @param windowInSecond
     * @return
     */
    public boolean sliderWindowAccess(final String key, final long maxCount, final int windowInSecond) {
        final String result = String.valueOf(
                stringRedisTemplate.execute(RedisLuaScript.SLIDER_WINDOW_COUNT, Collections.singletonList(key),
                        String.valueOf(maxCount), String.valueOf(windowInSecond),
                        String.valueOf(System.currentTimeMillis()),
                        System.currentTimeMillis() + "-" + IdsUtil.getNextStrId()
                ));
        return Objects.equals("1", result);
    }

}
