package com.ddf.boot.common.sentinel.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcTotalConfig;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sentinel的自动配置类
 * <p>
 * https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
 * <p>
 * https://github.com/alibaba/Sentinel/blob/master/sentinel-demo/sentinel-demo-spring-webmvc/src/main/java/com/alibaba/csp/sentinel/demo/spring/webmvc/config/InterceptorConfig.java
 *
 * 客户端引入这个模块之后，还需要在项目启动时添加虚拟机启动参数-Dcsp.sentinel.dashboard.server=106.15.10.135:8807 -Dproject.name=boot-quick
 *
 * csp.sentinel.dashboard.server sentinel控制台地址 ip:port的形式
 * project.name 项目名称，分类用，会生成一个菜单
 *
 * @author dongfang.ding
 * @date 2020/12/6 0006 23:40
 */
@Configuration
public class SentinelAutoConfiguration implements WebMvcConfigurer {

    /**
     * 如果是通过 Spring Cloud Alibaba 则不需要这个
     * https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
     *
     * @return
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * Web 适配，经测试这个也可以使用，不过这个是基于原生Servlet的
     * https://github.com/alibaba/Sentinel/wiki/%E4%B8%BB%E6%B5%81%E6%A1%86%E6%9E%B6%E7%9A%84%E9%80%82%E9%85%8D#web-servlet
     *
     *         <dependency>
     *             <groupId>com.alibaba.csp</groupId>
     *             <artifactId>sentinel-web-servlet</artifactId>
     *             <version>${sentinel.version}</version>
     *         </dependency>
     *
     * @return
     */
//    @Bean
//    public FilterRegistrationBean<CommonFilter> sentinelFilterRegistration() {
//        FilterRegistrationBean<CommonFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(new CommonFilter());
//        registration.addUrlPatterns("/*");
//        registration.setName("sentinelFilter");
//        registration.setOrder(1);
//        return registration;
//    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add Sentinel interceptor
        addSpringMvcInterceptor(registry);
        addSpringMvcTotalInterceptor(registry);
    }


    /**
     * 接口请求后会被拦截到控制台的---簇点链路上， 可以不适用@SentinelResource注解， 区别是， 如果接口调用链上没有生命是的@SentinelResource资源，
     * 那么就将最外层接口入口层（即方法的@RequestMapping）作为资源且不会往下生成资源链。而如果@RequestMapping方法调用了某个@SentinelResource资源，
     * 则会在入口层往下生成资源链路
     *
     * 格式如下
     * 无资源映射
     *  /helloA
     *
     * 有资源映射
     *  /helloB
     *      -- hello(如果这个方法声明了@SentinelResource的话)
     *
     *
     * @param registry
     */
    private void addSpringMvcInterceptor(InterceptorRegistry registry) {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();

        // Depending on your situation, you can choose to process the BlockException via
        // the BlockExceptionHandler or throw it directly, then handle it
        // in Spring web global exception handler.

        // config.setBlockExceptionHandler((request, response, e) -> { throw e; });

        // Use the default handler.
        config.setBlockExceptionHandler(new SimpleBlockExceptionHandler());

        // Custom configuration if necessary
        config.setHttpMethodSpecify(true);
        // By default web context is true, means that unify web context(i.e. use the default context name),
        // in most scenarios that's enough, and it could reduce the memory footprint.
        // If set it to false, entrance contexts will be separated by different URLs,
        // which is useful to support "chain" relation flow strategy.
        // We can change it and view different result in `Resource Chain` menu of dashboard.
        config.setWebContextUnify(true);
        config.setOriginParser(request -> request.getHeader("S-user"));

        // Add sentinel interceptor
        registry.addInterceptor(new SentinelWebInterceptor(config)).addPathPatterns("/**");
    }

    /**
     * 这个是所有的请求下面都会包含这个资源， 意思是将系统所有接口统一为同一个资源名称，然后对统一的资源名称做一个限流
     * @param registry
     */
    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        // Config
        SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();

        // Custom configuration if necessary
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container");
        config.setTotalResourceName("my-spring-mvc-total-url-request");

        // Add sentinel interceptor
        registry.addInterceptor(new SentinelWebTotalInterceptor(config)).addPathPatterns("/**");
    }
}
