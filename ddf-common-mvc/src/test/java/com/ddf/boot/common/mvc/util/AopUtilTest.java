package com.ddf.boot.common.mvc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AopUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class AopUtilTest {

    @Test
    @DisplayName("应解析 JoinPoint 的类和方法签名")
    void shouldResolveJoinPointClassAndMethod() throws NoSuchMethodException {
        JoinPoint joinPoint = mockJoinPoint(DemoService.class, "process",
                new Class<?>[] {String.class, DemoPayload.class}, new Object[] {"demo", new DemoPayload("payload")},
                new String[] {"plainText", "body"});

        assertEquals(DemoService.class, AopUtil.getJoinPointClass(joinPoint));
        assertEquals("process", AopUtil.getJoinPointMethod(joinPoint).getName());
        assertNotNull(AopUtil.getAnnotation(joinPoint, DemoAnnotation.class));
    }

    @Test
    @DisplayName("应提取所有参数与可序列化参数")
    void shouldResolveAllAndSerializableArguments() throws NoSuchMethodException {
        ServletRequest servletRequest = mock(ServletRequest.class);
        JoinPoint joinPoint = mockJoinPoint(DemoService.class, "serialize",
                new Class<?>[] {String.class, DemoPayload.class, ServletRequest.class},
                new Object[] {"demo", new DemoPayload("payload"), servletRequest},
                new String[] {"plainText", "body", "request"});

        Map<Class<?>, Object> args = AopUtil.getArgs(joinPoint);
        Map<String, Object> allParamMap = AopUtil.getAllParamMap(joinPoint);
        Map<String, Object> serializableParamMap = AopUtil.getSerializableParamMap(joinPoint);

        assertEquals("demo", args.get(String.class));
        assertTrue(args.containsKey(DemoPayload.class));
        assertEquals(3, allParamMap.size());
        assertEquals(2, serializableParamMap.size());
        assertTrue(serializableParamMap.containsKey("plainText"));
        assertTrue(serializableParamMap.containsKey("body"));
        assertTrue(!serializableParamMap.containsKey("request"));
        assertNull(AopUtil.getRequestBodyParamObj(joinPoint));
    }

    private JoinPoint mockJoinPoint(Class<?> type, String methodName, Class<?>[] parameterTypes, Object[] args,
            String[] parameterNames) throws NoSuchMethodException {
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = type.getMethod(methodName, parameterTypes);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(signature.getDeclaringType()).thenReturn(type);
        when(signature.getName()).thenReturn(methodName);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterTypes()).thenReturn(parameterTypes);
        when(signature.getParameterNames()).thenReturn(parameterNames);

        return joinPoint;
    }

    @DemoAnnotation
    static class DemoService {

        @DemoAnnotation
        public void process(String plainText, @RequestBody DemoPayload body) {
        }

        public void serialize(String plainText, @RequestBody DemoPayload body, ServletRequest request) {
        }
    }


    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface DemoAnnotation {
    }


    record DemoPayload(String value) {
    }
}
