package com.ddf.boot.common.sentinel.config;

import com.alibaba.csp.sentinel.adapter.servlet.CommonFilter;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel的自动配置类
 *
 * https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
 *
 * @author dongfang.ding
 * @date 2020/12/6 0006 23:40
 */
@Configuration
public class SentinelAutoConfiguration {

    /**
     * 如果是通过 Spring Cloud Alibaba 则不需要这个
     * https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
     * @return
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * Web 适配
     * https://github.com/alibaba/Sentinel/wiki/%E4%B8%BB%E6%B5%81%E6%A1%86%E6%9E%B6%E7%9A%84%E9%80%82%E9%85%8D#web-servlet
     * @return
     */
    @Bean
    public FilterRegistrationBean<CommonFilter> sentinelFilterRegistration() {
        FilterRegistrationBean<CommonFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CommonFilter());
        registration.addUrlPatterns("/*");
        registration.setName("sentinelFilter");
        registration.setOrder(1);
        return registration;
    }
}
