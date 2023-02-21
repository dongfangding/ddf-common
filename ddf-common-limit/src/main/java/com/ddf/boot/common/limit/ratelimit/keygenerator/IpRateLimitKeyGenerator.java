package com.ddf.boot.common.limit.ratelimit.keygenerator;

import com.ddf.boot.common.core.util.WebUtil;
import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import com.ddf.boot.common.redis.constant.ApplicationNamedKeyGenerator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * <p>ip限流规则key生成器</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 14:03
 */
public class IpRateLimitKeyGenerator implements RateLimitKeyGenerator {

    /**
     * 该类bean_name
     *
     */
    public static final String BEAN_NAME = "ipRateLimitKeyGenerator";

    /**
     * 限流key的生成接口
     *
     * @param joinPoint
     * @param annotation
     * @param properties
     * @return
     */
    @Override
    public String generateKey(JoinPoint joinPoint, RateLimit annotation, RateLimitProperties properties) {
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint.getSignature()
                .getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        return ApplicationNamedKeyGenerator.genKey(getPrefix(), WebUtil.getHost(), currentClass.getName(), currentMethod.getName());
    }
}
