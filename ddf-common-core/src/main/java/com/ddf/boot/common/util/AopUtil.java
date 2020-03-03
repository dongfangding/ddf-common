package com.ddf.boot.common.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * aop获取相关属性的方法工具类$
 * <p>
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2019/12/20 0020 10:24
 */
public class AopUtil {


    /**
     * 获取在指定方法上的注解，如果方法上没有就到类上去找
     *
     * @param joinPoint
     * @param targetAnnotation
     * @return T
     * @author dongfang.ding
     * @date 2019/12/20 0020 10:29
     **/
    public static <T extends Annotation> T getAnnotation(ProceedingJoinPoint joinPoint, Class<T> targetAnnotation) throws NoSuchMethodException {
        Class clazz = joinPoint.getSignature().getDeclaringType();
        String methodName = joinPoint.getSignature().getName();
        Class[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
        Method method = clazz.getMethod(methodName, parameterTypes);
        T annotation = method.getAnnotation(targetAnnotation);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(clazz, targetAnnotation);
        }
        return annotation;
    }


    /**
     * 获取指定类型的参数
     *
     * @param joinPoint
     * @return java.util.Map<java.lang.Class < ?>,java.lang.Object>
     * @author dongfang.ding
     * @date 2019/12/20 0020 11:28
     **/
    public static Map<Class<?>, Object> getArgs(ProceedingJoinPoint joinPoint) {
        if (joinPoint == null) {
            return Collections.emptyMap();
        }
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return Collections.emptyMap();
        }
        Map<Class<?>, Object> argsMap = new HashMap<>(args.length);
        for (Object arg : args) {
            argsMap.put(arg.getClass(), arg);
        }
        return argsMap;
    }
}
