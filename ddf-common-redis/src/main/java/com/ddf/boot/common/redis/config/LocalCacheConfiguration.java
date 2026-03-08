package com.ddf.boot.common.redis.config;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>enable caching local cache config</p >
 *
 * @author robot
 */
@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class LocalCacheConfiguration {

    /**
     * 具体配置可参考 {@link com.github.benmanes.caffeine.cache.CaffeineSpec}
     */
    @Bean(name = "localCacheManager")
    public CacheManager localCacheManager(CacheProperties cacheProperties) {
        CacheProperties.Caffeine caffeine = cacheProperties.getCaffeine();

        String spec = ObjectUtil.defaultIfBlank(caffeine.getSpec(), "maximumSize=10000,expireAfterWrite=5m,softValues");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheSpecification(spec);

        return cacheManager;
    }

}
