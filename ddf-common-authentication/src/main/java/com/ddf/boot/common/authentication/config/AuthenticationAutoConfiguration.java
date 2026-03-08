package com.ddf.boot.common.authentication.config;

import com.ddf.boot.common.authentication.filter.AuthenticateTokenFilter;
import com.ddf.boot.common.authentication.interfaces.TokenCustomizeCheckService;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import com.ddf.boot.common.authentication.interfaces.impl.DefaultTokenCheckServiceImpl;
import com.ddf.boot.common.authentication.interfaces.impl.TokenCacheImpl;
import com.ddf.boot.common.core.authentication.TokenCache;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块的自动配置类类
 *
 * @author dongfang.ding
 * @since 2020/8/16 0016 13:59
 */
@Configuration
@ComponentScan(basePackages = "com.ddf.boot.common.authentication")
public class AuthenticationAutoConfiguration {

    @Bean
    @ConditionalOnBean(AuthenticateTokenFilter.class)
    @ConditionalOnMissingBean(TokenCustomizeCheckService.class)
    public TokenCustomizeCheckService defaultTokenCheckServiceImpl(AuthenticationProperties authenticationProperties,
            UserClaimService userClaimService) {
        return new DefaultTokenCheckServiceImpl(authenticationProperties, userClaimService);
    }

    /**
     * @param authenticationProperties 参数
     * @param environmentHelper 参数
     */
    @Bean
    @ConditionalOnBean(AuthenticateTokenFilter.class)
    @ConditionalOnMissingBean(TokenCache.class)
    public TokenCache tokenCacheImpl(AuthenticationProperties authenticationProperties,
            EnvironmentHelper environmentHelper) {
        return new TokenCacheImpl(authenticationProperties, environmentHelper);
    }
}