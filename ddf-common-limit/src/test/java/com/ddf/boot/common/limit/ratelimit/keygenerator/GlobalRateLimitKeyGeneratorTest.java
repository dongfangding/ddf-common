package com.ddf.boot.common.limit.ratelimit.keygenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.limit.ratelimit.annotation.RateLimit;
import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * GlobalRateLimitKeyGenerator 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class GlobalRateLimitKeyGeneratorTest {

    @Test
    @DisplayName("应使用类名和方法名生成限流 key")
    void shouldGenerateKeyUsingClassAndMethodName() throws NoSuchMethodException {
        GlobalRateLimitKeyGenerator generator = new GlobalRateLimitKeyGenerator();
        JoinPoint joinPoint = mockJoinPoint(DemoController.class, "submit");

        String key = generator.generateKey(joinPoint,
                DemoController.class.getMethod("submit").getAnnotation(RateLimit.class), new RateLimitProperties());

        assertTrue(key.contains("rate_limit"));
        assertTrue(key.contains(DemoController.class.getName()));
        assertTrue(key.contains("submit"));
        assertEquals("rate_limit", generator.getPrefix());
    }

    private JoinPoint mockJoinPoint(Class<?> type, String methodName) throws NoSuchMethodException {
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = type.getMethod(methodName);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(type);
        when(signature.getName()).thenReturn(methodName);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterTypes()).thenReturn(method.getParameterTypes());
        when(signature.getParameterNames()).thenReturn(new String[0]);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        return joinPoint;
    }

    static class DemoController {
        @RateLimit
        public void submit() {
        }
    }
}
