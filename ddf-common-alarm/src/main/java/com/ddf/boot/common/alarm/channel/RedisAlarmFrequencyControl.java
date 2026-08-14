package com.ddf.boot.common.alarm.channel;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisAlarmFrequencyControl implements AlarmFrequencyControl {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisAlarmFrequencyControl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryAcquire(String alarmKey) {
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent("alarm:frequency:" + alarmKey, "1", Duration.ofMinutes(5));
        return Boolean.TRUE.equals(ok);
    }
}
