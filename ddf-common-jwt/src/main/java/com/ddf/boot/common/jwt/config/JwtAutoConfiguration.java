package com.ddf.boot.common.jwt.config;

import com.ddf.boot.common.jwt.filter.JwtAuthorizationTokenFilter;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * jwt模块的自动配置类类
 *
 * @author dongfang.ding
 * @date 2020/8/16 0016 13:59
 */
@Configuration
@ComponentScan(basePackages = "com.ddf.boot.common.jwt")
public class JwtAutoConfiguration implements WebMvcConfigurer {

    @Autowired(required = false)
    private JwtAuthorizationTokenFilter jwtAuthorizationTokenFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (Objects.nonNull(jwtAuthorizationTokenFilter)) {
            registry.addInterceptor(jwtAuthorizationTokenFilter).addPathPatterns("/**");
        }
    }
}
