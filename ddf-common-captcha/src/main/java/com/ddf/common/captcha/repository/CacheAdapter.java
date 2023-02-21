package com.ddf.common.captcha.repository;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>description</p >
 *
 * 适配器
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/11/06 16:27
 */
public class CacheAdapter {

    private static StringRedisTemplate stringRedisTemplate;

    public CacheAdapter(StringRedisTemplate stringRedisTemplate) {
        CacheAdapter.stringRedisTemplate = stringRedisTemplate;
    }

    public static StringRedisTemplate getTemplate() {
        return stringRedisTemplate;
    }

}
