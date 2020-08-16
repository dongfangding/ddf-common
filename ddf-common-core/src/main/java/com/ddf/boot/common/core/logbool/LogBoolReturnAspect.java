package com.ddf.boot.common.core.logbool;

import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 有一个约定，使用方必须返回BoolReturn, 在这个对象中表名对数据修改的接口，有没有修改成功，因为有的接口
 * 对操作进行了幂等判断，已经执行过的不在执行，这种数据就需要在日志中表名接口被调用，但是没有修改数据，
 * 就需要接口中遵循约定返回BoolReturn这个对象
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/12 15:40
 */
@Aspect
@Slf4j
@Component
@ConditionalOnBean(LogBoolReturnAction.class)
public class LogBoolReturnAspect {

    @Autowired(required = false)
    private LogBoolReturnAction logBoolReturnAction;

    @Pointcut(value = "@annotation(com.ddf.boot.common.core.logbool.LogBoolReturn)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 当前业务异常不处理，直接抛出去
        Object proceed = joinPoint.proceed();
        LogBoolReturnResult logBoolReturnResult = new LogBoolReturnResult();
        boolean isBoolReturn = false;
        if (proceed instanceof BoolReturn) {
            isBoolReturn = true;
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogBoolReturn annotation = method.getAnnotation(LogBoolReturn.class);

            logBoolReturnResult.setClassName(className)
                    .setMethodName(methodName)
                    .setLogName(annotation.logName())
                    .setParam(JsonUtil.asString(AopUtil.getParamMap(joinPoint)));
            logBoolReturnResult.setBoolReturn((BoolReturn) proceed);
        }

        // 将数据暴露出去，让使用方实现业务
        if (isBoolReturn && logBoolReturnAction != null) {
            logBoolReturnAction.doAction(logBoolReturnResult);
        }
        return proceed;
    }
}