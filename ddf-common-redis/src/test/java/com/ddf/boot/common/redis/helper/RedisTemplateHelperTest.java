package com.ddf.boot.common.redis.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.redis.request.ZRevRangeBizRankingQuery;
import com.ddf.boot.common.redis.request.ZSetAddDoubleWithMaxCheckCommand;
import java.math.BigDecimal;
import java.util.Date;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * RedisTemplateHelper 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class RedisTemplateHelperTest {

    @Test
    @DisplayName("incrementKeyExpire 应解析脚本返回值")
    void shouldParseIncrementKeyExpireResult() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        RedissonClient redissonClient = org.mockito.Mockito.mock(RedissonClient.class);
        RedisTemplateHelper helper = new RedisTemplateHelper(stringRedisTemplate, redissonClient);

        when(stringRedisTemplate.execute(any(), anyList(), eq("1"), eq("60"))).thenReturn("3");

        Long result = helper.incrementKeyExpire("demo:key", 60L);

        assertEquals(3L, result);
        verify(stringRedisTemplate).execute(any(), anyList(), eq("1"), eq("60"));
    }

    @Test
    @DisplayName("incrementKeyExpireAt 应校验过期时间并解析结果")
    void shouldValidateExpireAtAndParseResult() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        RedissonClient redissonClient = org.mockito.Mockito.mock(RedissonClient.class);
        RedisTemplateHelper helper = new RedisTemplateHelper(stringRedisTemplate, redissonClient);
        Date expireAt = new Date(System.currentTimeMillis() + 30_000L);

        when(stringRedisTemplate.execute(
            any(),
            anyList(),
            eq("1"),
            eq(String.valueOf(expireAt.getTime() / 1000))
        )).thenReturn("5");

        Long result = helper.incrementKeyExpireAt("demo:key", expireAt);

        assertEquals(5L, result);
        verify(stringRedisTemplate).execute(
            any(),
            anyList(),
            eq("1"),
            eq(String.valueOf(expireAt.getTime() / 1000))
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> helper.incrementKeyExpireAt("demo:key", new Date(System.currentTimeMillis() - 1_000L))
        );
        assertEquals("过期时间不能早于当前时间", exception.getMessage());
    }

    @Test
    @DisplayName("stringIncrWithLimitCheckException 在限流时应抛出业务异常")
    void shouldThrowBusinessExceptionWhenStringIncrHitsLimit() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        RedissonClient redissonClient = org.mockito.Mockito.mock(RedissonClient.class);
        RedisTemplateHelper helper = new RedisTemplateHelper(stringRedisTemplate, redissonClient);

        when(stringRedisTemplate.execute(any(), anyList(), eq("1"), eq("5"), eq("30")))
            .thenReturn("{\"limited\":true,\"currentCount\":5,\"maxCount\":5}");

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> helper.stringIncrWithLimitCheckException(
                "demo:key",
                1L,
                5L,
                30L,
                () -> "never",
                BaseErrorCallbackCode.REQUEST_TOO_MANY
            )
        );

        assertEquals(BaseErrorCallbackCode.REQUEST_TOO_MANY.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("stringIncrWithLimitCheckException 未限流时应执行 supplier")
    void shouldReturnSupplierResultWhenStringIncrDoesNotHitLimit() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        RedissonClient redissonClient = org.mockito.Mockito.mock(RedissonClient.class);
        RedisTemplateHelper helper = new RedisTemplateHelper(stringRedisTemplate, redissonClient);

        when(stringRedisTemplate.execute(any(), anyList(), eq("1"), eq("5"), eq("30")))
            .thenReturn("{\"limited\":false,\"currentCount\":3,\"maxCount\":5}");

        Supplier<String> supplier = () -> "passed";
        String result = helper.stringIncrWithLimitCheckException(
            "demo:key",
            1L,
            5L,
            30L,
            supplier,
            BaseErrorCallbackCode.REQUEST_TOO_MANY
        );

        assertEquals("passed", result);
    }

    @Test
    @DisplayName("stringTtlIncrWithLimit 应兼容空返回与满额返回")
    void shouldParseStringTtlIncrWithLimitResult() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        RedissonClient redissonClient = org.mockito.Mockito.mock(RedissonClient.class);
        RedisTemplateHelper helper = new RedisTemplateHelper(stringRedisTemplate, redissonClient);

        when(stringRedisTemplate.execute(any(), anyList(), eq("10"), eq("60")))
            .thenReturn("");

        assertEquals(0, helper.stringTtlIncrWithLimit("demo:key", 10, 60).getTtl());
        assertFalse(helper.stringTtlIncrWithLimit("demo:key", 10, 60).isFull());

        when(stringRedisTemplate.execute(any(), anyList(), eq("15"), eq("60")))
            .thenReturn("1-45");

        assertTrue(helper.stringTtlIncrWithLimit("demo:key", 15, 60).isFull());
        assertEquals(45, helper.stringTtlIncrWithLimit("demo:key", 15, 60).getTtl());
    }

    @Test
    @DisplayName("zset 业务查询与写入应校验 scoreFactory")
    void shouldValidateScoreFactoryForBizRankingOperations() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        RedissonClient redissonClient = org.mockito.Mockito.mock(RedissonClient.class);
        RedisTemplateHelper helper = new RedisTemplateHelper(stringRedisTemplate, redissonClient);

        ZSetAddDoubleWithMaxCheckCommand addCommand = new ZSetAddDoubleWithMaxCheckCommand();
        addCommand.setScoreFactory(3);
        addCommand.setScore(BigDecimal.ONE);

        IllegalArgumentException addException = assertThrows(
            IllegalArgumentException.class,
            () -> helper.zSetAddWithMaxCheckSupportBiz(addCommand)
        );
        assertEquals("scoreFactory must be a multiple of 10 or 1", addException.getMessage());

        ZRevRangeBizRankingQuery query = new ZRevRangeBizRankingQuery();
        query.setScoreFactory(6);

        IllegalArgumentException queryException = assertThrows(
            IllegalArgumentException.class,
            () -> helper.zSetRevRangeBizRankingQuery(query)
        );
        assertEquals("scoreFactory must be a multiple of 10 or 1", queryException.getMessage());
    }

    @Test
    @DisplayName("calcPointScoreByTime 应返回递减小数分值")
    void shouldCalculatePointScoreByTime() {
        BigDecimal result = RedisTemplateHelper.calcPointScoreByTime(12345L);

        assertEquals(new BigDecimal("0.87655"), result.stripTrailingZeros());
    }
}
