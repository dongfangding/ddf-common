package com.ddf.boot.common.limit.ratelimit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>限流忽略标记注解， 原始是{@link RateLimit}可用在类上， 因此提供一个具体方法的忽略标记注解</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 11:56
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitIgnore {

}
