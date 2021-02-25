package com.ddf.boot.common.limit.ratelimit.keygenerator;

import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import org.aspectj.lang.JoinPoint;

/**
 * <p>限流key的生成器, 用来控制目标方法限流的粒度</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 14:02
 */
public interface RateLimitKeyGenerator {

    String RATE_LIMIT_PREFIX = "rate_limit";

    /**
     * 限流key的固定前缀
     *
     * @return
     */
    default String getPrefix() {
        return RATE_LIMIT_PREFIX;
    }

    /**
     * 限流key的生成接口
     *
     * @param joinPoint
     * @param annotation
     * @param properties
     * @return
     */
    String generateKey(JoinPoint joinPoint, RateLimit annotation, RateLimitProperties properties);
}
