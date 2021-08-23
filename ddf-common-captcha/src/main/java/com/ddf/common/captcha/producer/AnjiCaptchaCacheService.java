package com.ddf.common.captcha.producer;

import com.anji.captcha.service.CaptchaCacheService;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 滑块或点选文字验证码实现
 *
 * <p>https://github.com/anji-plus/captcha</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/12 10:46
 */
public class AnjiCaptchaCacheService implements CaptchaCacheService {

    private StringRedisTemplate stringRedisTemplate;

    public AnjiCaptchaCacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 设置验证码过期时间
     *
     * @param key
     * @param value
     * @param expiresInSeconds
     */
    @Override
    public void set(String key, String value, long expiresInSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        final Boolean aBoolean = stringRedisTemplate.hasKey(key);
        return Objects.nonNull(aBoolean) && aBoolean;
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Long increment(String key, long val) {
        return stringRedisTemplate.opsForValue().increment(key,val);
    }

    /**
     * 缓存类型-local/redis/memcache/..
     * 通过java SPI机制，接入方可自定义实现类
     *
     * @return
     */
    @Override
    public String type() {
        return "redis";
    }
}
