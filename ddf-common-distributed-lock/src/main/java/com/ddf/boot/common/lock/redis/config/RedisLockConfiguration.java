package com.ddf.boot.common.lock.redis.config;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.redis.impl.RedisDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化redis分布式锁配置类
 *
 * @author dongfang.ding
 **/
@Configuration
@Slf4j
@EnableConfigurationProperties(value = {DistributedLockRedisProperties.class})
@ConditionalOnProperty(prefix = "customs.distributed.lock.redis", value = "enable", havingValue = "true")
public class RedisLockConfiguration {

    private final DistributedLockRedisProperties distributedLockRedisProperties;

    public RedisLockConfiguration(DistributedLockRedisProperties distributedLockRedisProperties) {
        this.distributedLockRedisProperties = distributedLockRedisProperties;
    }

    /**
     * 注册redis锁实现类
     *
     * @param redissonClient
     * @return
     */
    @Bean(name = RedisDistributedLock.BEAN_NAME)
    public DistributedLock redisDistributedLock(@Autowired RedissonClient redissonClient) {
        return new RedisDistributedLock(redissonClient, distributedLockRedisProperties);
    }
}
