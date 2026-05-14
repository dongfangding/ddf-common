package com.ddf.boot.common.authentication.interfaces;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>允许使用方决定使用StringRedisTemplate</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/05/27 19:58
 */
public interface RedisTemplateSupport {

    /**
     * 实现该接口方法可以决定使用指定的StringRedisTemplate
     */
    StringRedisTemplate getStringRedisTemplate();
}
