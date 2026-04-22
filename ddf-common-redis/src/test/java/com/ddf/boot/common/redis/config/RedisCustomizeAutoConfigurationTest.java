package com.ddf.boot.common.redis.config;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.redis.helper.GeoHelper;
import com.ddf.boot.common.redis.helper.RedisCommandHelper;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCustomizeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisCustomizeAutoConfiguration.class))
            .withUserConfiguration(RedisSupportConfiguration.class);

    @Test
    void shouldRegisterRedisHelperBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RedisTemplateHelper.class);
            assertThat(context).hasSingleBean(RedisCommandHelper.class);
            assertThat(context).hasSingleBean(GeoHelper.class);
        });
    }

    @Test
    void shouldCreateDefaultRedisTemplatesWithExpectedSerializers() {
        contextRunner.run(context -> {
            RedisTemplate<?, ?> redisTemplate = context.getBean("redisTemplate", RedisTemplate.class);
            StringRedisTemplate stringRedisTemplate = context.getBean("stringRedisTemplate", StringRedisTemplate.class);

            assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
            assertThat(redisTemplate.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
            assertThat(stringRedisTemplate.getConnectionFactory()).isNotNull();
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class RedisSupportConfiguration {

        @Bean
        EnvironmentHelper environmentHelper() {
            return Mockito.mock(EnvironmentHelper.class);
        }

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return Mockito.mock(RedisConnectionFactory.class);
        }

        @Bean
        RedissonClient redissonClient() {
            return Mockito.mock(RedissonClient.class);
        }
    }
}
