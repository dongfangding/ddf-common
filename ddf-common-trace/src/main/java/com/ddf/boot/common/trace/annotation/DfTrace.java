package com.ddf.boot.common.trace.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>一些不符合默认规则的拦截通过这个注解可以直接被拦截,或者一些高级功能也必须通过该注解指定</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/23 09:46
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DfTrace {

    /**
     * 是否打印返回值
     *
     * @return
     */
    boolean traceReturn() default false;

    /**
     * 由于在同一个方法中可能还会调用另外的接口，而且也满足规则， 所以会存在多个service都会trace的过程，这里定义深度， 如果超过深度了，
     * 不再继续进行参数解析等控制
     *
     * 注意这个计数从第一次拦截开始，每次有新拦截都会递增，即使是跨服务也会将计数传递过去
     *
     * @return
     */
    int traceDeepNum() default 10;

}
