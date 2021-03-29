package com.ddf.boot.common.limit.ratelimit.annotation;

import com.ddf.boot.common.limit.ratelimit.keygenerator.RateLimitKeyGenerator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>限流标记</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 11:56
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 令牌桶的key生成规则, 主要是区分限流key的粒度问题， 如方法级别， 用户的方法级别，甚至是热点参数级别
     *
     * @see RateLimitKeyGenerator
     * @return
     */
    String keyGenerator() default "";

    /**
     * 条件表达式， 更可能的场景是该接口不限流，但是满足某些特定参数或者场景之后触发限流, 仅支持Spel表达式
     *
     * @return
     */
    String condition() default "";

    /**
     * 限流的最大令牌桶数量
     *
     * @return
     */
    int max() default 0;

    /**
     * 令牌桶恢复速率,单位秒
     *
     * @return
     */
    int rate() default 0;
}
