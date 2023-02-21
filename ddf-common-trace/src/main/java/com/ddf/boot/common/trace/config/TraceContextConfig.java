package com.ddf.boot.common.trace.config;

import com.ddf.boot.common.trace.aop.IdentityInterceptor;
import com.ddf.boot.common.trace.aop.TraceAspect;
import com.ddf.boot.common.trace.extra.EmptyIdentityCollectServiceImpl;
import com.ddf.boot.common.trace.extra.IdentityCollectService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>trace相关配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/20 18:20
 */
@Configuration
public class TraceContextConfig implements WebMvcConfigurer {

    /**
     * 空的获取用户信息的实现
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdentityCollectService emptyIdentityCollectServiceImpl() {
        return new EmptyIdentityCollectServiceImpl();
    }


    @Bean
    public IdentityInterceptor identityInterceptor() {
        return new IdentityInterceptor(emptyIdentityCollectServiceImpl());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(identityInterceptor()).addPathPatterns("/**");
    }

    /**
     * 参数拦截aop实现
     *
     * @return
     */
    @Bean
    public TraceAspect traceAspect() {
        return new TraceAspect();
    }
}
