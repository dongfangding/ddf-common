package com.ddf.boot.common.redis.config;

import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/12/11 11:06
 */
@Configuration
public class RedisCustomizeAutoConfiguration {

    /**
     * 注册redis扩展方法类
     * @param stringRedisTemplate
     * @return
     */
    @Bean
    public RedisTemplateHelper redisTemplateHelper(@Autowired StringRedisTemplate stringRedisTemplate) {
        return new RedisTemplateHelper(stringRedisTemplate);
    }
}
