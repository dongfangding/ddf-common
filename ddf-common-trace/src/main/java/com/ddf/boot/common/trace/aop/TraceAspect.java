package com.ddf.boot.common.trace.aop;

import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.trace.annotation.DfTrace;
import com.ddf.boot.common.trace.context.QlTraceContext;
import com.ddf.boot.common.trace.context.TraceProcess;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.omg.CORBA.SystemException;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/20 18:01
 */
@Aspect
@Slf4j
public class TraceAspect {

    /**
     * 拦截所有的controller
     */
    @Pointcut("execution(* com..controller..*(..))")
    private void controller() {
    }

    /**
     * 拦截所有的dubbo调用，但是如果调用传递到下层服务的时候， 加入Service这个注解拦截规则会导致本地方法也会被拦截，可是如果不加，上层
     * 应用第一次传递下来也不会被拦截，
     */
    @Pointcut("@within(com.alibaba.dubbo.config.annotation.Service)) || @within(com.alibaba.dubbo.config.annotation.Reference)")
    private void dubbo() {
    }

    /**
     * 由于参数拦截以及trace的规则过多，这里必须添加自定义注解才真正拦截
     */
    @Pointcut("@annotation(com.ddf.boot.common.trace.annotation.DfTrace)")
    private void qileTrace() {

    }

    /**
     * 拦截实现类
     *
     * FIXME 确定使用哪种方式？
     * 1. @Around(value = "(controller() || dubbo() || qileTrace()) && qileTrace()")
     *      这种方式，必须在满足规则的情况下还要添加自定义注解，即使在方法入口添加了，后续调用如果想打印都也得加注解，如果跨服务也必须每个地方都加
     *
     * 2. @Around(value = "(controller() || dubbo() || qileTrace())")
     *      这种方式， 然后内部通过traceId来标识，同一个请求有一个方法被拦截的时候添加了拦截注解则后续调用都生效，跨服务也会把这个参数传递到后面服务中
     *
     *  为了使用方便，暂时使用第二种方式
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around(value = "(controller() || dubbo() || qileTrace())")
    public Object handler(ProceedingJoinPoint joinPoint) throws Throwable {
        final long beforeTime = System.currentTimeMillis();
        Class<?> pointClass = AopUtil.getJoinPointClass(joinPoint);
        MethodSignature pointMethod = AopUtil.getJoinPointMethod(joinPoint);
        final TraceProcess traceProcess = ObjectUtils.defaultIfNull(QlTraceContext.getContextDomain().getTraceProcess(), TraceProcess.defaultInstance());
        final int currentTraceDepth = traceProcess.incrementAndGetDepth();
        final DfTrace annotation = pointMethod.getMethod().getAnnotation(DfTrace.class);
        if (Objects.nonNull(annotation)) {
            traceProcess.setTraceFlag(Boolean.TRUE);
        }
        final boolean traceFlag = traceProcess.isTraceFlag();
        // 判断trace次数是否已经超限
        boolean shouldTrace = traceFlag && (Objects.isNull(annotation) || currentTraceDepth <= annotation.traceDeepNum());
        try {
            String paramJson = "";
            // 本来想直接在执行成功后，连返回值处理逻辑放一起处理，反正失败的时候参数也会打印，但这里不打印，多次拦截，后面调用的方法
            // 由于先出栈， 日志反而先出来，容易造成误解，所以还是先打印出来了
            if (shouldTrace) {
                paramJson = AopUtil.serializeParam(joinPoint);
                log.info("{}#{}请求参数: {}", pointClass.getName(), pointMethod.getName(), paramJson);
            }
            Object result = joinPoint.proceed();
            if (!shouldTrace) {
                return result;
            }
            long consumerTime = System.currentTimeMillis() - beforeTime;
            if (Objects.nonNull(annotation) && annotation.traceReturn()) {
                log.info("{}#{}请求参数: {}, 执行返回结果: {}, 共耗时: [{}ms]", pointClass.getName(), pointMethod.getName(),
                        paramJson, JsonUtil.asString(result), consumerTime
                );
            } else {
                log.info("{}#{}请求参数: {}, 执行结果不打印, 共耗时: [{}ms]", pointClass.getName(), pointMethod.getName(),
                        paramJson , consumerTime
                );
            }
            return result;
        } catch (Exception throwable) {
            if (!(throwable instanceof SystemException)) {
                log.error("{}#{}请求参数: {}, 执行出现异常！异常消息==>", pointClass.getName(), pointMethod.getName(),
                        AopUtil.serializeParam(joinPoint), throwable);
            }
            throw throwable;
        }
    }
}
