package com.ddf.boot.common.core.authentication;

import java.lang.reflect.Field;

/**
 * 测试辅助：清理 hutool {@code SpringUtil} 的静态上下文。
 *
 * <p>加载 {@code CoreAutoConfiguration}（间接经由 {@code SpringContextHolder} 的
 * {@code @EnableSpringUtil}）后，hutool 的 {@code SpringUtil} 会持有当前 Spring 上下文的静态引用。
 * {@code ApplicationContextRunner} 在回调结束后会关闭上下文，但该静态引用不会被自动清除，
 * 从而污染同一 JVM 中依赖 {@code SpringContextHolder#getBeanWithStatic} 判空行为的其他测试
 * （如 {@code IdsUtilTest}）。因此每个测试结束后需调用 {@link #reset()} 清理。</p>
 */
final class SpringUtilCleaner {

    private static final String[] FIELD_NAMES = {"beanFactory", "applicationContext"};

    private SpringUtilCleaner() {
    }

    static void reset() {
        try {
            Class<?> clazz = Class.forName("cn.hutool.extra.spring.SpringUtil");
            for (String fieldName : FIELD_NAMES) {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(null, null);
            }
        } catch (Exception e) {
            throw new IllegalStateException("无法重置 SpringUtil 静态上下文", e);
        }
    }
}
