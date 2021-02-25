package com.ddf.boot.common.limit.ratelimit.annotation;

import com.ddf.boot.common.limit.ratelimit.config.RateLimitRegistrar;
import com.ddf.boot.common.limit.ratelimit.extra.RateLimitPropertiesCollect;
import com.ddf.boot.common.limit.ratelimit.handler.RateLimitAspect;
import com.ddf.boot.common.limit.ratelimit.keygenerator.GlobalRateLimitKeyGenerator;
import com.ddf.boot.common.limit.ratelimit.keygenerator.RateLimitKeyGenerator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * <p>开启限流功能， 目的
 * 1. 可开关， 在压测时方便直接关闭限流
 * 2. 可最小化开发，支持全局限流定义，个别接口在{@link RateLimit}在覆盖特殊处理
 * /p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 11:45
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {RateLimitRegistrar.class})
public @interface EnableRateLimit {

    /**
     * 令牌桶的key生成规则, 主要是区分限流key的粒度问题， 如方法级别， 用户的方法级别，甚至是热点参数级别
     *
     * @see RateLimitKeyGenerator
     * @return
     */
    String keyGenerators() default GlobalRateLimitKeyGenerator.BEAN_NAME;

    /**
     * 是否是spring-cloud环境并使用@RequestScope刷新特性。
     * 如果是这个，则外部提供属性类对属性进行接收和支持， 当前模块仅提供接口允许外部实现将最新值传入进来
     *
     * 原因是由于当前模块的依赖问题， 在这个模块中不准备依赖cloud的依赖。如果想要使用动态刷新特性， 外部实现属性注入和刷新配置类，
     * 可然后实现接口{@link RateLimitPropertiesCollect}来返回实时刷新值
     *
     * @see RateLimitAspect
     * @see RateLimitPropertiesCollect
     * @return
     */
    boolean cloudRefresh() default false;

    /**
     * 限流的最大令牌桶数量
     *
     * @return
     */
    int max() default 1;

    /**
     * 令牌桶恢复速率
     *
     * @return
     */
    int rate() default 1;
}
