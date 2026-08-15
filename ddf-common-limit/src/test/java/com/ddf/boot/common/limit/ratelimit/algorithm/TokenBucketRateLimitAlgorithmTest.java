package com.ddf.boot.common.limit.ratelimit.algorithm;

import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TokenBucketRateLimitAlgorithm} 委托逻辑测试。
 *
 * @author snowball
 */
@ExtendWith(MockitoExtension.class)
class TokenBucketRateLimitAlgorithmTest {

    @Mock
    private RedisTemplateHelper redisTemplateHelper;

    @Test
    void shouldReturnTokenBucketAlgorithmName() {
        TokenBucketRateLimitAlgorithm algorithm = new TokenBucketRateLimitAlgorithm(redisTemplateHelper);

        assertThat(algorithm.getAlgorithm()).isEqualTo("tokenBucket");
    }

    @Test
    void shouldDelegateAndReturnTrue() {
        when(redisTemplateHelper.tokenBucketRateLimitAcquire("order:123", 10, 5)).thenReturn(true);
        TokenBucketRateLimitAlgorithm algorithm = new TokenBucketRateLimitAlgorithm(redisTemplateHelper);

        boolean result = algorithm.tryAcquire("order:123", 10, 5);

        assertThat(result).isTrue();
        verify(redisTemplateHelper).tokenBucketRateLimitAcquire("order:123", 10, 5);
    }

    @Test
    void shouldDelegateAndReturnFalse() {
        when(redisTemplateHelper.tokenBucketRateLimitAcquire("order:123", 10, 5)).thenReturn(false);
        TokenBucketRateLimitAlgorithm algorithm = new TokenBucketRateLimitAlgorithm(redisTemplateHelper);

        boolean result = algorithm.tryAcquire("order:123", 10, 5);

        assertThat(result).isFalse();
        verify(redisTemplateHelper).tokenBucketRateLimitAcquire("order:123", 10, 5);
    }
}
