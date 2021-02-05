package com.ddf.boot.common.redis.config;

import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import com.ddf.boot.common.redis.validator.RedisRepeatableValidator;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
@AutoConfigureAfter(value = {RedissonAutoConfiguration.class})
public class RedisCustomizeAutoConfiguration {

    /**
     * 注册redis扩展方法类
     *
     * @param stringRedisTemplate
     * @return
     */
    @Bean
    public RedisTemplateHelper redisTemplateHelper(StringRedisTemplate stringRedisTemplate,
            RedissonClient redissonClient) {
        return new RedisTemplateHelper(stringRedisTemplate, redissonClient);
    }

    /**
     * 基于redis实现的防重复提交校验器
     *
     * @return
     */
    @Bean(name = RedisRepeatableValidator.BEAN_NAME)
    public RedisRepeatableValidator repeatableValidator(@Autowired StringRedisTemplate stringRedisTemplate) {
        return new RedisRepeatableValidator(stringRedisTemplate);
    }
}
