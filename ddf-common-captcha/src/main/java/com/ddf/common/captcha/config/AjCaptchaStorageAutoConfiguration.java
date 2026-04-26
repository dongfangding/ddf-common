package com.ddf.common.captcha.config;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import com.ddf.boot.common.redis.helper.RedisCommandHelper;
import com.ddf.common.captcha.producer.AnJiCaptchaCacheService;
import com.ddf.common.captcha.properties.AjCaptchaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储策略自动配置.
 */
@Configuration
public class AjCaptchaStorageAutoConfiguration {

    @Bean(name = "AjCaptchaCacheService")
    public CaptchaCacheService captchaCacheService(AjCaptchaProperties ajCaptchaProperties,
            RedisCommandHelper redisCommandHelper) {
        CaptchaCacheService captchaCacheService = new AnJiCaptchaCacheService(redisCommandHelper);
        CaptchaServiceFactory.cacheService.put(ajCaptchaProperties.getCacheType().name(), captchaCacheService);
        return captchaCacheService;
    }
}
