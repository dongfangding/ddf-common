package com.ddf.boot.common.redis.config;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import lombok.SneakyThrows;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/12/11 11:06
 */
@Configuration
@AutoConfigureAfter(value = {RedissonAutoConfiguration.class})
@EnableConfigurationProperties({RedissonCustomizeProperties.class})
public class RedisCustomizeAutoConfiguration implements RedissonAutoConfigurationCustomizer {

    @Autowired
    private RedissonCustomizeProperties redissonCustomizeProperties;

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
     * Customize the RedissonClient configuration.
     *
     * @param configuration the {@link Config} to customize
     */
    @SneakyThrows
    @Override
    public void customize(Config configuration) {
        if (StrUtil.isNotBlank(redissonCustomizeProperties.getCodec())) {
            configuration.setCodec((Codec) Class.forName(redissonCustomizeProperties.getCodec()).newInstance());
        } else {
            configuration.setCodec(new JsonJacksonCodec());
        }
    }
}
