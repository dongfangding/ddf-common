package com.ddf.boot.common.authentication.interfaces;

import com.ddf.boot.common.core.helper.SpringContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>允许使用方决定使用StringRedisTemplate</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/27 19:58
 */
public interface RedisTemplateSupport {

    /**
     * 默认使用的StringRedisTemplate
     *
     * @return
     */
    static StringRedisTemplate defaultRedisTemplate() {
        return SpringContextHolder.getBean(StringRedisTemplate.class);
    }

    /**
     * 实现该接口方法可以决定使用指定的StringRedisTemplate
     *
     * @return
     */
    StringRedisTemplate getStringRedisTemplate();
}
