package com.ddf.common.captcha.producer;

import com.anji.captcha.service.CaptchaCacheService;
import com.ddf.boot.common.redis.helper.RedisCommandHelper;

/**
 * 滑块或点选文字验证码缓存实现
 * <p>https://github.com/anji-plus/captcha</p>
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/07/12 10:46
 */
public class AnJiCaptchaCacheService implements CaptchaCacheService {

    private final RedisCommandHelper redisCommandHelper;

    public AnJiCaptchaCacheService(RedisCommandHelper redisCommandHelper) {
        this.redisCommandHelper = redisCommandHelper;
    }

    /**
     * 设置验证码过期时间
     *
     * @param key 目标键
     * @param value 参数值
     * @param expiresInSeconds 过期秒数
     */
    @Override
    public void set(String key, String value, long expiresInSeconds) {
        redisCommandHelper.set(key, value, expiresInSeconds);
    }

    /**
     * @param key 目标键
     */
    @Override
    public boolean exists(String key) {
        return redisCommandHelper.hasKey(key);
    }

    /**
     * @param key 目标键
     */
    @Override
    public void delete(String key) {
        redisCommandHelper.delete(key);
    }

    /**
     * @param key 目标键
     */
    @Override
    public String get(String key) {
        return redisCommandHelper.get(key);
    }

    /**
     * @param key 目标键
     * @param val 参数
     */
    @Override
    public Long increment(String key, long val) {
        return redisCommandHelper.incrBy(key, val);
    }

    /**
     * 缓存类型-local/redis/memcache/..
     * 通过 java SPI 机制，接入方可自定义实现类
     *
     * @return 类型
     */
    @Override
    public String type() {
        return "redis";
    }
}
