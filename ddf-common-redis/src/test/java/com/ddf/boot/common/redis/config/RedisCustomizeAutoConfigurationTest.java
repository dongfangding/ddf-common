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
