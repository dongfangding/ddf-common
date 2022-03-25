package com.ddf.boot.common.redis.config;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.redis.helper.GeoHelper;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import lombok.SneakyThrows;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/12/11 11:06
 */
@Configuration
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

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    @Primary
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.setStringSerializer(new StringRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    /**
     * 注册geo帮助类
     *
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public GeoHelper geoHelper(RedissonClient redissonClient) {
        return new GeoHelper(redissonClient);
    }

    /**
     * Customize the RedissonClient configuration.
     *
     * @param configuration the {@link Config} to customize
     */
    @SneakyThrows
    @Override
    public void customize(Config configuration) {
        if (configuration.isSentinelConfig()) {
            // fixme 临时解决，最后还是没看懂，先按照提示取消校验
            // https://stackoverflow.com/questions/53665923/spring-redisson-sentinel-error-at-least-two-sentinels-are-required
            // https://github.com/redisson/redisson/issues/2788
            configuration.useSentinelServers().setCheckSentinelsList(false);
        }
        if (StrUtil.isNotBlank(redissonCustomizeProperties.getCodec())) {
            configuration.setCodec((Codec) Class.forName(redissonCustomizeProperties.getCodec()).newInstance());
        } else {
            configuration.setCodec(new JsonJacksonCodec());
        }
    }
}
