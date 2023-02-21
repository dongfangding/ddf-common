package com.ddf.boot.common.redis.config;

import cn.hutool.core.util.ObjectUtil;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * <p>enable caching redis cache config</p >
 * <p></p>
 *
 * 开启缓存， 默认是事务提交之后执行， 可选condition进行判断是否执行
 * <pre class="code">
 * &#64;Cacheable(cacheNames = SpringCacheManager.CacheName.CLUB_INFO_DB,
 *      condition = "#property1 == #property2",
 *      key = "T(com.ddf.boot.common.redis.constant.SpringCacheManager).
 *          genCacheKeyDemo(#property1, #property1)")
 * public Object getByProperties(String property1, String property2) {
 *      return xxxDao.getByProperties(property1, property2);
 * }
 * </pre>
 * <p></p>
 *
 * 牵扯到更新缓存的地方删除缓存
 * <pre class="code">
 * &#64;Caching(evict = {
 *      &#64;CacheEvict(cacheNames = SpringCacheManager.CacheName.CLUB_INFO_DB,
 *          key = "T(com.company.sian.club.constants.SpringCacheManager).
 *              genClubInfoDb(#clubInfo.clubId)")
 * })
 * public Object updateObject(String property1, String property2) {
 *      return xxxDao.update(property1, property2);
 * }
 * </pre>
 *
 *
 *
 *
 * @author Mitchell
 * @version 1.0
 * @date 2021/1/14 10:21
 */
@EnableCaching
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CacheProperties.class)
public class RedisCacheManagerConfiguration {

    @Slf4j
    static class Logger {
    }

    @Bean
    @Primary
    public CacheManager redisCacheManager(@Value("${spring.application.name}") String applicationName,
            CacheProperties cacheProperties, RedisConnectionFactory connectionFactory) {

        // 如何测试lockingRedisCacheWriter和nonLockingRedisCacheWriter的区别???
        RedisCacheWriter cacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);

        RedisCacheConfiguration defaultCacheConfig = createConfiguration(cacheProperties, applicationName);

        RedisCacheManager.RedisCacheManagerBuilder redisCacheManagerBuilder =
                RedisCacheManager.RedisCacheManagerBuilder.fromCacheWriter(cacheWriter)
                        .cacheDefaults(defaultCacheConfig);

        return redisCacheManagerBuilder.build();
    }

    private RedisCacheConfiguration createConfiguration(CacheProperties cacheProperties, String applicationName) {
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        // Key prefix.
        String prefixKeys = ObjectUtil.defaultIfNull(redisProperties.getKeyPrefix(), applicationName + ":");
        // Entry expiration. By default the entries never expire.
        Duration ttl = ObjectUtil.defaultIfNull(redisProperties.getTimeToLive(), Duration.ofSeconds(60 * 60));

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()))
                .entryTtl(ttl)
                .prefixKeysWith(prefixKeys);

        // Allow caching null values.
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
            Logger.log.warn("======> ${spring.cache.redis.cache-null-values} is false, not recommended. <======");
        }
        // Whether to use the key prefix when writing to Redis.
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
            Logger.log.warn("======> ${spring.cache.redis.use-key-prefix} is false, not recommended. <======");
        }

        Logger.log.info("======> spring redis cache , prefix keys={}, ttl={} <======", prefixKeys, ttl);
        return config;
    }
}
