package com.ddf.boot.common.core.config;

import com.ddf.boot.common.core.helper.ThreadBuilderHelper;
import com.ddf.boot.common.core.resolver.MultiArgumentResolverMethodProcessor;
import com.ddf.boot.common.core.resolver.QueryParamArgumentResolver;
import com.ddf.boot.common.core.util.SpringContextHolder;
import java.util.List;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 框架核心配置类
 * <p>
 * 主要这里一定要实现{@link WebMvcConfigurer}，该接口已经提供了默认实现，而且{@link @EnableSpringDataWebSupport}
 * 提供的{@link SpringDataWebConfiguration}就是实现了该接口来添加相对应的参数解析器和消息转换器，如果这里处置不当，
 * 会覆盖该注解提供的功能，如{@link PageableHandlerMethodArgumentResolver}
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2018/12/8
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
@EnableCaching
public class CoreWebConfig implements WebMvcConfigurer {

    /**
     * 注册解析器
     *
     * @return
     */
    @Bean
    public QueryParamArgumentResolver queryParamArgumentResolver() {
        return new QueryParamArgumentResolver();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new MappingJackson2HttpMessageConverter());
    }

    /**
     * 配置拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    }

    /**
     * 配置允许所有请求跨域
     *
     * @param registry
     */
    @Override
    @Deprecated
    public void addCorsMappings(CorsRegistry registry) {
        // 这种由于拦截器的顺序问题无法处理项目内部有自定义拦截器且内部出现异常的问题
        // registry.addMapping("/**").allowCredentials(false).allowedHeaders("*").allowedOrigins("*").allowedMethods("*");
    }

    /**
     * 处理全局跨域
     * https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-cors-filter
     *
     * @return
     */
    @Bean
    public CorsFilter corsRegistration() {
        CorsConfiguration config = new CorsConfiguration();
        // Possibly...
        // config.applyPermitDefaultValues()
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 配置静态资源映射路径
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    /**
     * 配置自定义参数解析器
     *
     * @param resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(SpringContextHolder.getBean(MultiArgumentResolverMethodProcessor.class));
        resolvers.add(queryParamArgumentResolver());
    }

    /**
     * 默认线程池
     *
     * @return
     */
    @Bean
    @Primary
    public ThreadPoolTaskExecutor defaultThreadPool() {
        return ThreadBuilderHelper.buildThreadExecutor("default-thread-pool", 60, 1000);
    }

    /**
     * 定时任务调度线程池
     *
     * @return
     */
    @Bean
    @Primary
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler threadPoolScheduler = new ThreadPoolTaskScheduler();
        threadPoolScheduler.setThreadNamePrefix("scheduledExecutorService-");
        threadPoolScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolScheduler.setRemoveOnCancelPolicy(true);
        return threadPoolScheduler;
    }
}
