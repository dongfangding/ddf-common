package com.ddf.boot.common.limit.repeatable.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.limit.repeatable.annotation.Repeatable;
import com.ddf.boot.common.limit.repeatable.config.RepeatableProperties;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * LocalRepeatableValidator 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class LocalRepeatableValidatorTest {

    @Test
    @DisplayName("相同请求在间隔期内应被判定为重复提交")
    void shouldRejectSameRequestWithinInterval() throws NoSuchMethodException {
        LocalRepeatableValidator validator = new LocalRepeatableValidator();
        RepeatableProperties properties = new RepeatableProperties();
        properties.setInterval(1000L);
        JoinPoint joinPoint = mockJoinPoint(new Object[] {"demo"});
        Repeatable repeatable = DemoService.class.getMethod("submit", String.class).getAnnotation(Repeatable.class);

        boolean first = validator.check(joinPoint, repeatable, "1001", properties);
        boolean second = validator.check(joinPoint, repeatable, "1001", properties);

        assertTrue(first);
        assertFalse(second);
    }

    @Test
    @DisplayName("超过间隔或参数变化后应允许再次提交")
    void shouldAllowNewRequestAfterIntervalOrValueChange() throws Exception {
        LocalRepeatableValidator validator = new LocalRepeatableValidator();
        RepeatableProperties properties = new RepeatableProperties();
        properties.setInterval(30L);
        Repeatable repeatable = DemoService.class.getMethod("submit", String.class).getAnnotation(Repeatable.class);

        JoinPoint firstJoinPoint = mockJoinPoint(new Object[] {"demo-a"});
        JoinPoint secondJoinPoint = mockJoinPoint(new Object[] {"demo-b"});

        assertTrue(validator.check(firstJoinPoint, repeatable, "1001", properties));
        Thread.sleep(40L);
        assertTrue(validator.check(secondJoinPoint, repeatable, "1001", properties));
    }

    private JoinPoint mockJoinPoint(Object[] args) throws NoSuchMethodException {
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = DemoService.class.getMethod("submit", String.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(signature.getDeclaringType()).thenReturn(DemoService.class);
        when(signature.getName()).thenReturn("submit");
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterTypes()).thenReturn(method.getParameterTypes());
        when(signature.getParameterNames()).thenReturn(new String[] {"value"});
        return joinPoint;
    }

    static class DemoService {
        @Repeatable(interval = 20L)
        public void submit(String value) {
        }
    }
}
