package com.ddf.boot.common.limit.ratelimit.keygenerator;

import com.ddf.boot.common.core.util.UserContextUtil;
import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import com.ddf.boot.common.redis.constant.ApplicationNamedKeyGenerator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * <p>身份级别的限流key生成规则, 即不同身份对接口访问次数的频率控制是分开限制的</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 14:29
 */
public class IdentityRateLimitKeyGenerator implements RateLimitKeyGenerator {

    /**
     * 该类的bean_name
     *
     */
    public static final String BEAN_NAME = "identityRateLimitKeyGenerator";

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
        // 身份标识
        String identityNo = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(UserContextUtil.getUserId(),
                UserContextUtil.getCredit()), getPrefix());
        // 获取当前拦截类
        final Class<?> currentClass = joinPoint.getSignature()
                .getDeclaringType();
        // 获取当前拦截方法
        MethodSignature currentMethod = (MethodSignature) joinPoint.getSignature();
        return ApplicationNamedKeyGenerator.genKey(getPrefix(), identityNo, currentClass.getName(), currentMethod.getName());
    }
}
