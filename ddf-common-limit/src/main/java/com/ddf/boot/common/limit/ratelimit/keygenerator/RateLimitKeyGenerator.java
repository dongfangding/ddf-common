package com.ddf.boot.common.limit.ratelimit.keygenerator;

import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import org.aspectj.lang.JoinPoint;

/**
 * <p>限流key的生成器, 用来控制目标方法限流的粒度</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/02/24 14:02
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
     * @param joinPoint joinpoint参数
     * @param annotation annotation参数
     * @param properties properties参数
     * @return
     */
    String generateKey(JoinPoint joinPoint, RateLimit annotation, RateLimitProperties properties);
}
