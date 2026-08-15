package com.ddf.boot.common.alarm.channel;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link RedisAlarmFrequencyControl} 静默窗口逻辑测试。
 *
 * @author snowball
 */
@ExtendWith(MockitoExtension.class)
class RedisAlarmFrequencyControlTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisAlarmFrequencyControl newControl() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        return new RedisAlarmFrequencyControl(stringRedisTemplate);
    }

    @Test
    void shouldAcquireWhenKeyNotPresent() {
        when(valueOperations.setIfAbsent(eq("alarm:frequency:demo"), eq("1"), eq(Duration.ofMinutes(5))))
                .thenReturn(true);

        boolean result = newControl().tryAcquire("demo");

        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectWhenKeyAlreadyInWindow() {
        when(valueOperations.setIfAbsent(eq("alarm:frequency:demo"), eq("1"), eq(Duration.ofMinutes(5))))
                .thenReturn(false);

        boolean result = newControl().tryAcquire("demo");

        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectWhenSetIfAbsentReturnsNull() {
        when(valueOperations.setIfAbsent(eq("alarm:frequency:demo"), eq("1"), eq(Duration.ofMinutes(5))))
                .thenReturn(null);

        boolean result = newControl().tryAcquire("demo");

        assertThat(result).isFalse();
    }
}
