package com.ddf.common.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 拦截指定的请求为某些注解功能提供支持，目前支持功能如下
 * <ul>
 *     <li>{@link EnableLogAspect}</li>
 * </ul>
 * 
 * @author dongfang.ding on 2018/10/9
 */
@Aspect
public class AccessLogAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String BEAN_NAME = "accessLogAspect";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LogAspectConfiguration logAspectConfiguration;

    @Autowired(required = false)
    private SlowEventAction slowEventAction;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private ThreadLocal<Long> beforeTime = new ThreadLocal<>();

    @Pointcut(value = "execution(public * com..controller..*(..))")
    public void pointCut() {}

    /**
     * before处理日志和封装用户信息
     * @param joinPoint
     */
    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        beforeTime.set(System.currentTimeMillis());
        logBefore(joinPoint);
    }

    /**
     * 请求成功执行并返回值后后打印日志
     * @param joinPoint
     * @param result
     */
    @AfterReturning(pointcut = "pointCut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        if (!logAspectConfiguration.isEnableLogAspect()) {
            return;
        }
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        long consumerTime = System.currentTimeMillis() - beforeTime.get();
        logger.info("[{}.{}]{}..........: ({})", className, methodName, "方法执行结束，成功返回值,共耗时(" + consumerTime + "ms)", result != null ? result.toString() : "");
        beforeTime.remove();
        dealSlowTimeHandler(className, methodName, consumerTime);
    }

    /**
     * 如果接口耗时超过预设值，提供一个异步回调接口给使用者实现处理逻辑
     * @param className
     * @param methodName
     * @param consumerTime
     */
    private void  dealSlowTimeHandler(String className, String methodName, long consumerTime) {
        long slowTime = logAspectConfiguration.getSlowTime();
        if (consumerTime > slowTime && slowEventAction != null && !checkIgnore(className)) {
            // 需要使用方自己去实现doAction接口接收参数自定义自己的处理机制
            SlowEventAction.SlowEvent slowEvent = new SlowEventAction.SlowEvent(className, methodName, consumerTime, slowTime);
            logger.info("{}-{}耗时{}，准备执行处理回调。。。。", className, methodName, consumerTime);
            taskExecutor.execute(() -> slowEventAction.doAction(slowEvent));
        }
    }

    /**
     * 判断是否忽略处理当前类
     * @param className
     * @return
     */
    private boolean checkIgnore(String className) {
        String[] ignore = logAspectConfiguration.getIgnore();
        if (ignore != null && ignore.length > 0) {
            for (String aClass : ignore) {
                if (aClass.equals(className) || className.startsWith(aClass)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 请求出现异常打印日志
     * @param joinPoint
     * @param exception
     */
    @AfterThrowing(pointcut = "pointCut()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        if (!logAspectConfiguration.isEnableLogAspect()) {
            return;
        }
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("[{}.{}]{}..........: ({})", className, methodName, "方法执行出现异常",
                exception.toString()  == null ? "" : exception.toString());
    }



    /**
     * 记录方法入参
     * @param joinPoint {@link JoinPoint}
     */
    private void logBefore(JoinPoint joinPoint) {
        if (!logAspectConfiguration.isEnableLogAspect()) {
            return;
        }
        Map<String, Object> paramsMap = new HashMap<>();
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        String str = "";
        if (parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                String value = joinPoint.getArgs()[i] != null ? joinPoint.getArgs()[i].toString() : "null";
                paramsMap.put(parameterNames[i], value);
            }
            try {
                str = objectMapper.writeValueAsString(paramsMap);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        logger.info("[{}.{}方法执行，参数列表===>({})]", className, methodName, str);
    }


}
