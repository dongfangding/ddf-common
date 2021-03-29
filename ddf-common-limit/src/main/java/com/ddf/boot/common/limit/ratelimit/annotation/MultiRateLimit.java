package com.ddf.boot.common.limit.ratelimit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>允许一个方法存在多个限流规则</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/29 20:26
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiRateLimit {

    /**
     * 定义多个限流规则
     *
     * @return
     */
    RateLimit[] rules() default {};
}
