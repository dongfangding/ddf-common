package com.ddf.boot.common.authentication.config;

import com.ddf.boot.common.authentication.filter.AuthenticateTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证模块的自动配置类类
 *
 * @author dongfang.ding
 * @since 2020/8/16 0016 13:59
 */
@Configuration
@RequiredArgsConstructor
public class AuthenticationWebConfiguration implements WebMvcConfigurer {

    private final ObjectProvider<AuthenticateTokenFilter> authenticateTokenFilter;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        authenticateTokenFilter.ifAvailable(filter -> {
            registry.addInterceptor(filter)
                    .addPathPatterns("/**");
        });
    }
}
