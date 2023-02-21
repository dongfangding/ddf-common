package com.ddf.common.ons.redis;

import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * ONS Redis自动配置
 *
 * @author SteveGuo
 * @date 2020-08-20 3:11 PM
 */
@Configuration
@EnableConfigurationProperties(OnsRedisProperties.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class OnsRedisAutoConfiguration {

    @Bean
    public StringRedisTemplate onsRedisTemplate(OnsRedisProperties onsRedisProperties) {
        RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig(onsRedisProperties);
        LettuceClientConfiguration clientConfiguration = getLettuceClientConfiguration(onsRedisProperties);
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(standaloneConfig, clientConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();
        return new StringRedisTemplate(lettuceConnectionFactory);
    }


    private RedisStandaloneConfiguration getStandaloneConfig(OnsRedisProperties redis) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redis.getHost());
        config.setPort(redis.getPort());
        config.setPassword(RedisPassword.of(redis.getPassword()));
        config.setDatabase(redis.getDatabase());
        return config;
    }

    private LettuceClientConfiguration getLettuceClientConfiguration(OnsRedisProperties redis) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        OnsRedisProperties.Pool pool = redis.getLettuce().getPool();
        if (pool == null) {
            builder = LettuceClientConfiguration.builder();
        } else {
            builder = new PoolBuilderFactory().createBuilder(pool);
        }
        if (redis.getTimeout() != null) {
            builder.commandTimeout(redis.getTimeout());
        }
        if (redis.getLettuce() != null) {
            OnsRedisProperties.Lettuce lettuce = redis.getLettuce();
            if (lettuce.getShutdownTimeout() != null
                    && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(redis.getLettuce().getShutdownTimeout());
            }
        }
        builder.clientResources(DefaultClientResources.create());
        return builder.build();
    }

    /**
     * Inner class to allow optional commons-pool2 dependency.
     */
    private static class PoolBuilderFactory {

        public LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(OnsRedisProperties.Pool pool) {
            return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(pool));
        }

        private GenericObjectPoolConfig getPoolConfig(OnsRedisProperties.Pool pool) {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(pool.getMaxActive());
            config.setMaxIdle(pool.getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            if (pool.getMaxWait() != null) {
                config.setMaxWaitMillis(pool.getMaxWait().toMillis());
            }
            return config;
        }

    }

}
