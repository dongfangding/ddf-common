package com.ddf.common.captcha.producer;

import com.anji.captcha.service.CaptchaCacheService;
import com.ddf.common.captcha.repository.CacheAdapter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 滑块或点选文字验证码实现
 *
 * <p>https://github.com/anji-plus/captcha</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/12 10:46
 */
public class AnJiCaptchaCacheService implements CaptchaCacheService {

    /**
     * 必须保留空构造， 有load SPI
     */
    public AnJiCaptchaCacheService() {
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
        CacheAdapter.getTemplate().opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        final Boolean aBoolean = CacheAdapter.getTemplate().hasKey(key);
        return Objects.nonNull(aBoolean) && aBoolean;
    }

    @Override
    public void delete(String key) {
        CacheAdapter.getTemplate().delete(key);
    }

    @Override
    public String get(String key) {
        return CacheAdapter.getTemplate().opsForValue().get(key);
    }

    @Override
    public Long increment(String key, long val) {
        return CacheAdapter.getTemplate().opsForValue().increment(key,val);
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
