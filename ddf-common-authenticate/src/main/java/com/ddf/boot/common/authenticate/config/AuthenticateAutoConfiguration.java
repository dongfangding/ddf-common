package com.ddf.boot.common.authenticate.config;

import com.ddf.boot.common.authenticate.filter.AuthenticateTokenFilter;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证模块的自动配置类类
 *
 * @author dongfang.ding
 * @date 2020/8/16 0016 13:59
 */
@Configuration
@ComponentScan(basePackages = "com.ddf.boot.common.authenticate")
public class AuthenticateAutoConfiguration implements WebMvcConfigurer {

    @Autowired(required = false)
    private AuthenticateTokenFilter authenticateTokenFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (Objects.nonNull(authenticateTokenFilter)) {
            registry.addInterceptor(authenticateTokenFilter).addPathPatterns("/**");
        }
    }
}
