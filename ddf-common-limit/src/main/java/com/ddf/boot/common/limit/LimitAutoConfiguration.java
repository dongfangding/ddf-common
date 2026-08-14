package com.ddf.boot.common.limit;

import com.ddf.boot.common.limit.ratelimit.algorithm.RateLimitAlgorithm;
import com.ddf.boot.common.limit.ratelimit.algorithm.TokenBucketRateLimitAlgorithm;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/02/23 17:56
 */
@AutoConfiguration
public class LimitAutoConfiguration {

    /**
     * 注册默认令牌桶限流算法，bean name 与 {@link TokenBucketRateLimitAlgorithm#ALGORITHM} 一致，
     * 以便被切面中的 {@code Map<String, RateLimitAlgorithm>} 按名称收集。
     */
    @Bean(name = TokenBucketRateLimitAlgorithm.ALGORITHM)
    @ConditionalOnMissingBean(name = TokenBucketRateLimitAlgorithm.ALGORITHM)
    public RateLimitAlgorithm tokenBucketRateLimitAlgorithm(RedisTemplateHelper redisTemplateHelper) {
        return new TokenBucketRateLimitAlgorithm(redisTemplateHelper);
    }
}
