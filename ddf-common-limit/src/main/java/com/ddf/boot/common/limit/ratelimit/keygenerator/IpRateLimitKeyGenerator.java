package com.ddf.boot.common.limit.ratelimit.keygenerator;

import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import com.ddf.boot.common.mvc.util.AopUtil;
import com.ddf.boot.common.mvc.util.WebUtil;
import com.ddf.boot.common.redis.constant.ApplicationNamedKeyGenerator;
import org.aspectj.lang.JoinPoint;

/**
 * <p>ip限流规则key生成器</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/02/24 14:03
 */
public class IpRateLimitKeyGenerator implements RateLimitKeyGenerator {

    /**
     * 该类bean_name
     */
    public static final String BEAN_NAME = "ipRateLimitKeyGenerator";

    /**
     * 限流key的生成接口
     *
     * @param joinPoint joinpoint参数
     * @param annotation annotation参数
     * @param properties properties参数
     */
    @Override
    public String generateKey(JoinPoint joinPoint, RateLimit annotation, RateLimitProperties properties) {
        return ApplicationNamedKeyGenerator.genKey(getPrefix(), WebUtil.getHost(),
                AopUtil.getJoinPointClass(joinPoint).getName(), AopUtil.getJoinPointMethod(joinPoint).getName());
    }
}
