package com.ddf.boot.common.redis.helper;

import com.ddf.boot.common.redis.request.RateLimitRequest;
import com.ddf.boot.common.redis.script.RedisRateLimitScript;
import java.util.Collections;
import java.util.Objects;
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

    public RedisTemplateHelper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 分布式限流
     *
     * @param request
     */
    public boolean rateLimitAcquire(RateLimitRequest request) {
        return Objects.equals(Boolean.TRUE, stringRedisTemplate.execute(new RedisRateLimitScript(), Collections.singletonList(request.getKey()),
            Integer.toString(request.getMax()), Integer.toString(request.getRate()), Long.toString(System.currentTimeMillis())));
    }

}
