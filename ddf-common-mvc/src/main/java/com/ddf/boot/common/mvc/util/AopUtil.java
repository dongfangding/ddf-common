package com.ddf.boot.common.mvc.util;

import cn.hutool.core.annotation.AnnotationUtil;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.api.util.JsonUtil;
import com.google.common.collect.Maps;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

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
     * 返回当前方法的参数map， 注意如果入参又不能序列化的对象也会返回，如果要用来做序列不要使用这个方法
     * 可以用{@link AopUtil#getSerializableParamMap(JoinPoint)}代替
     *
     * @param joinPoint
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author dongfang.ding
     * @date 2020/6/12 0012 18:46
     **/
    public static Map<String, Object> getAllParamMap(JoinPoint joinPoint) {
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
     * 返回可以序列化的当前方法的参数map
     *
     * @param joinPoint
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author dongfang.ding
     * @date 2020/6/12 0012 18:46
     **/
    public static Map<String, Object> getSerializableParamMap(JoinPoint joinPoint) {
        try {
            Map<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(joinPoint.getArgs().length);
            String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
            if (parameterNames.length > 0) {
                for (int i = 0; i < parameterNames.length; i++) {
                    Object value = joinPoint.getArgs()[i];
                    if (value instanceof ServletRequest || value instanceof ServletResponse || value instanceof MultipartFile || value instanceof MultipartFile[]) {
                        continue;
                    }
                    paramsMap.put(parameterNames[i], value);
                }
            }
            return paramsMap;
        } catch (Exception e) {
            throw new ServerErrorException(BaseErrorCallbackCode.SERIALIZE_PARAM_ERROR);
        }
    }


    /**
     * 获取参数中带@RequestBody的参数的对象的值
     *
     * @param joinPoint
     * @return
     */
    public static Object getRequestBodyParamObj(JoinPoint joinPoint) {
        Class<?>[] parameterClasses = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
        if (parameterClasses.length == 0) {
            return null;
        }
        for (int i = 0, length = parameterClasses.length; i < length; i++) {
            Class<?> clazz = parameterClasses[i];
            if (Objects.nonNull(clazz.getAnnotation(RequestBody.class))) {
                return joinPoint.getArgs()[i];
            }
        }
        return null;
    }


    /**
     * 序列化参数
     *
     * @param joinPoint
     * @return
     */
    public static String serializeParam(JoinPoint joinPoint) {
        return JsonUtil.asString(getSerializableParamMap(joinPoint));
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
        AnnotationUtil.setValue(annotation, name, value);
    }


    /**
     * 动态通过反射修改指定注解示例里的属性的值
     *
     * @param annotation   注解实例对象
     * @param nameValueMap 属性和值集合
     */
    @SneakyThrows
    public static void modifyAnnotationValue(Annotation annotation, Map<String, Object> nameValueMap) {
        if (CollectionUtils.isEmpty(nameValueMap)) {
            return;
        }
        nameValueMap.forEach((k, v) -> AnnotationUtil.setValue(annotation, k, v));
    }
}
