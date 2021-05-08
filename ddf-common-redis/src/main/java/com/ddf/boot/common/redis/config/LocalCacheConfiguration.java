package com.ddf.boot.common.redis.config;

import cn.hutool.core.util.ObjectUtil;
import com.ddf.boot.common.redis.constant.SpringCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
/**
 * <p>enable caching local cache config</p >
 *
 * @author Mitchell
 * @version 1.0
 * @date 2021/1/14 10:21
 */
@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class LocalCacheConfiguration {
 
    /**
     * 具体配置可参考 {@link com.github.benmanes.caffeine.cache.CaffeineSpec}
     * <p>
     * 本地缓存配置不建议配置过大空间，以及过期时间，通用配置建议如下
     * ${spring.cache.caffeine.spec} maximumSize=10000,expireAfterWrite=5m,softValues
     *
     * <ul>
     *   <li>{@code initialCapacity=[integer]}: sets {@link Caffeine#initialCapacity}.
     *   <li>{@code maximumSize=[long]}: sets {@link Caffeine#maximumSize}.
     *   <li>{@code maximumWeight=[long]}: sets {@link Caffeine#maximumWeight}.
     *   <li>{@code expireAfterAccess=[duration]}: sets {@link Caffeine#expireAfterAccess}.
     *   <li>{@code expireAfterWrite=[duration]}: sets {@link Caffeine#expireAfterWrite}.
     *   <li>{@code refreshAfterWrite=[duration]}: sets {@link Caffeine#refreshAfterWrite}.
     *   <li>{@code weakKeys}: sets {@link Caffeine#weakKeys}.
     *   <li>{@code weakValues}: sets {@link Caffeine#weakValues}.
     *   <li>{@code softValues}: sets {@link Caffeine#softValues}.
     *   <li>{@code recordStats}: sets {@link Caffeine#recordStats}.
     * </ul>
     *
     * <p>
     * 若有特定需求，建议另启 {@link SimpleCacheManager}
     */
    @Bean(name = SpringCacheManager.CacheManagerBeanName.LOCAL)
    public CacheManager localCacheManager(CacheProperties cacheProperties) {
        CacheProperties.Caffeine caffeine = cacheProperties.getCaffeine();
 
        String spec = ObjectUtil.defaultIfBlank(caffeine.getSpec(), "maximumSize=10000,expireAfterWrite=5m,softValues");
 
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
 
        cacheManager.setCacheSpecification(spec);
 
        return cacheManager;
    }
 
}
