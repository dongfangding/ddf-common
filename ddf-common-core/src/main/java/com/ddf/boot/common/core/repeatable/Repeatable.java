package com.ddf.boot.common.core.repeatable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>接口防重校验</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 11:35
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Repeatable {

    /**
     * 同一次请求的间隔时间，单位毫秒
     *
     * @return
     */
    long interval() default 100;

    /**
     * 验证方式， 默认使用{@link EnableRepeatable#globalValidator()}里指定的， 如果这里指定了，则使用这里的
     *
     * @see RepeatAspect
     * @see RepeatableValidator
     *
     * @return
     */
    String validator() default "";

    /**
     * 校验重复提交之后是否抛出异常， 默认抛出异常。
     *
     * 由于正常请求的响应可能会晚于后续重复提交的响应，
     * 而后续重复提交的响应返回异常会导致前端直接接收到异常直接渲染失败了， 除非对接错误码进行忽略；
     *
     * 而如果不抛出异常的话， 还是上面的问题， 重复提交的响应依然是快于正常接口的逻辑， 前端又会渲染成功的逻辑。
     * 因此将这个决定权下放，
     *
     * @return
     */
    boolean throwError() default true;
}
