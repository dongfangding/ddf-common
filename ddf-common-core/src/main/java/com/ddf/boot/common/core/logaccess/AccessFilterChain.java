package com.ddf.boot.common.core.logaccess;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * <p>由于拦截只需要拦截一次，但是可能会做不同的业务，如果每个业务都写一个aop，也是很烦的，这里在拦截后提供一个接口，会去调用实现</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/12/31 15:33
 */
public interface AccessFilterChain {


    /**
     * 实现的执行顺序
     *
     * order越小，优先级越高
     *
     * @return
     */
    Integer getOrder();


    /**
     * 将aop参数暴露， 允许多实现实现自己的拦截业务处理， 如数据校验、签名校验，用户校验交给原生的filter去做了
     *
     * @param joinPoint
     * @param pointClass
     * @param pointMethod
     * @return
     */
    boolean filter(ProceedingJoinPoint joinPoint, Class<?> pointClass, MethodSignature pointMethod);
}
