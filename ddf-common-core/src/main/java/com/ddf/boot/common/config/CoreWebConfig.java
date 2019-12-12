package com.ddf.boot.common.config;

import com.ddf.boot.common.helper.ThreadBuilderHelper;
import com.ddf.boot.common.resolver.QueryParamArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 *
 * 框架核心配置类
 *
 * 主要这里一定要实现{@link WebMvcConfigurer}，该接口已经提供了默认实现，而且{@link @EnableSpringDataWebSupport}
 * 提供的{@link SpringDataWebConfiguration}就是实现了该接口来添加相对应的参数解析器和消息转换器，如果这里处置不当，
 * 会覆盖该注解提供的功能，如{@link PageableHandlerMethodArgumentResolver}
 *
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
 *
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
@EnableCaching
public class CoreWebConfig implements WebMvcConfigurer {

	@Autowired
	private QueryParamArgumentResolver queryParamArgumentResolver;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(0, new MappingJackson2HttpMessageConverter());
	}

	/**
	 * 配置允许所有请求跨域
	 * @param registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowCredentials(true)
				.allowedHeaders("*")
				.allowedOrigins("*")
				.allowedMethods("*");
	}

	/**
	 * 配置静态资源映射路径
	 * @param registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	}

	/**
	 * 一个全局的用于格式化的工具类
	 * 如果一个方法内多次使用的话，最好还是自己new一个使用
	 * @return
	 */
	@Bean
	@Primary
	@Scope("prototype")
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	/**
	 * 配置自定义参数解析器
	 * @param resolvers
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(queryParamArgumentResolver);
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
	public ScheduledThreadPoolExecutor scheduledExecutorService() {
		ThreadFactory namedThreadFactory = new CustomizableThreadFactory("scheduledExecutorService-");
		ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
				namedThreadFactory);
		scheduledExecutorService.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1);
		return scheduledExecutorService;
	}
}
