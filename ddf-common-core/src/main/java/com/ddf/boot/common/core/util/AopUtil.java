package com.ddf.boot.common.core.util;

import com.google.common.collect.Maps;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

/**
 * aop获取相关属性的方法工具类$
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
    public static <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> targetAnnotation)
            throws NoSuchMethodException {
        Class<?> clazz = joinPoint.getSignature().getDeclaringType();
        String methodName = joinPoint.getSignature().getName();
        Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
        Method method = clazz.getMethod(methodName, parameterTypes);
        T annotation = method.getAnnotation(targetAnnotation);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(clazz, targetAnnotation);
        }
        return annotation;
    }


    /**
     * 获取当前拦截的类
     *
     * @param joinPoint
     * @return
     */
    public static Class<?> getJoinPointClass(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringType();
    }

    /**
     * 获取当前拦截的方法
     *
     * @param joinPoint
     * @return
     */
    public static MethodSignature getJoinPointMethod(JoinPoint joinPoint) {
        return (MethodSignature) joinPoint.getSignature();
    }


    /**
     * 获取指定类型的参数
     *
     * @param joinPoint
     * @return java.util.Map<java.lang.Class < ?>,java.lang.Object>
     * @author dongfang.ding
     * @date 2019/12/20 0020 11:28
     **/
    public static Map<Class<?>, Object> getArgs(JoinPoint joinPoint) {
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


    /**
     * 返回当前方法的参数map
     *
     * @param joinPoint
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author dongfang.ding
     * @date 2020/6/12 0012 18:46
     **/
    public static Map<String, Object> getParamMap(JoinPoint joinPoint) {
        Map<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(joinPoint.getArgs().length);
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        if (parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                Object value = joinPoint.getArgs()[i];
                paramsMap.put(parameterNames[i], value);
            }
        }
        return paramsMap;
    }

    /**
     * 动态通过反射修改指定注解实例里的属性的值， 这个是如果只有一个属性要修改时提供的简便方法
     *
     * @param annotation
     * @param name
     * @param value
     */
    @SneakyThrows
    public static void modifyAnnotationValue(Annotation annotation, String name, Object value) {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put(name, value);
        modifyAnnotationValue(annotation, valueMap);
    }


    /**
     * 动态通过反射修改指定注解示例里的属性的值
     *
     * @param annotation   注解实例对象
     * @param nameValueMap 属性和值集合
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static void modifyAnnotationValue(Annotation annotation, Map<String, Object> nameValueMap) {
        if (CollectionUtils.isEmpty(nameValueMap)) {
            return;
        }
        final InvocationHandler handler = Proxy.getInvocationHandler(annotation);
        // memberValues是注解代理类存储属性的固定属性值， 是个LinkedHashMap
        Field hField = handler.getClass().getDeclaredField("memberValues");
        hField.setAccessible(true);
        Map memberValues = (Map) hField.get(handler);
        nameValueMap.forEach(memberValues::put);
    }
}
