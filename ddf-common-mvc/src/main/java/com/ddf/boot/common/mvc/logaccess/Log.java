package com.ddf.boot.common.mvc.logaccess;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>标识要打印的日志</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2021/12/01 14:07
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 日志名称
     */
    String desc() default "";

    /**
     * 是否打印入参
     */
    boolean printParams() default true;

    /**
     * 是否打印结果
     */
    boolean printResult() default false;
}
