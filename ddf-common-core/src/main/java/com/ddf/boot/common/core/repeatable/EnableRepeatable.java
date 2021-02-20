package com.ddf.boot.common.core.repeatable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * <p>开启表单防重检查，定义该注解的目的是， 由于使用需要定义注解在一些类上和方法上，然后会散落到各个文件中，
 * 而这个规则的存在，可能会对压测造成一定的影响， 因此提供一个注解，可以全局开关，方便切换
 * </p >
 *
 * {@link Repeatable} 该注解可以标注在类上和方法上， 支持属性请进入代码查看， 然后就会按照既定规则执行防重复校验
 * {@link RepeatableIgnore} 由于{@link Repeatable}可以标注在类上，那么为了寻求简单，如果真的被应用到类上，而某个方法却不需要，则使用该注解标识跳过
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 12:54
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {RepeatableRegistrar.class})
public @interface EnableRepeatable {

    /**
     * 视作同一次请求的间隔时间，单位毫秒
     *
     * @return
     */
    long interval() default 1000;

    /**
     * 全局默认校验器， 这个是默认值， 可以在{@link Repeatable#validator()} 覆盖
     *
     * 可实现接口{@link RepeatableValidator} 完成自定义校验器实现
     *
     * @return
     */
    String globalValidator() default LocalRepeatableValidator.BEAN_NAME;
}
