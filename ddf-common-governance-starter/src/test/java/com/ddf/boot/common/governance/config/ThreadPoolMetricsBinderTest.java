package com.ddf.boot.common.governance.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ThreadPoolMetricsBinder 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class ThreadPoolMetricsBinderTest {

    @Test
    @DisplayName("应绑定符合规则的线程池指标")
    void shouldBindMetricsForMatchedExecutorBean() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("bizExecutor", ThreadPoolTaskExecutor.class, () -> createExecutor("biz-", 1));
        context.refresh();

        try {
            ThreadPoolTaskExecutor executor = context.getBean("bizExecutor", ThreadPoolTaskExecutor.class);
            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            GovernanceProperties properties = new GovernanceProperties();
            ThreadPoolMetricsBinder binder = new ThreadPoolMetricsBinder(context, registry, properties);

            binder.afterSingletonsInstantiated();

            Set<String> meterNames = registry.getMeters()
                    .stream()
                    .filter(meter -> "bizExecutor".equals(meter.getId().getTag("bean")))
                    .map(meter -> meter.getId().getName())
                    .collect(Collectors.toSet());

            assertFalse(meterNames.isEmpty());
            assertTrue(meterNames.stream().anyMatch(name -> name.contains("executor") || name.contains("pool")));
        } finally {
            context.getBean("bizExecutor", ThreadPoolTaskExecutor.class).shutdown();
            context.close();
        }
    }

    @Test
    @DisplayName("禁用或排除时不应绑定线程池指标")
    void shouldSkipBindingWhenDisabledOrExcluded() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("bizExecutor", ThreadPoolTaskExecutor.class, () -> createExecutor("biz-", 1));
        context.refresh();

        try {
            ThreadPoolTaskExecutor executor = context.getBean("bizExecutor", ThreadPoolTaskExecutor.class);
            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            GovernanceProperties properties = new GovernanceProperties();
            properties.getObservability().getThreadPool().setExcludeBeanNamePatterns(java.util.List.of("biz*"));
            ThreadPoolMetricsBinder binder = new ThreadPoolMetricsBinder(context, registry, properties);

            binder.afterSingletonsInstantiated();

            assertTrue(registry.getMeters().isEmpty());

            SimpleMeterRegistry disabledRegistry = new SimpleMeterRegistry();
            properties.getObservability().setEnabled(false);
            ThreadPoolMetricsBinder disabledBinder = new ThreadPoolMetricsBinder(context, disabledRegistry, properties);
            disabledBinder.afterSingletonsInstantiated();

            assertTrue(disabledRegistry.getMeters().isEmpty());
        } finally {
            context.getBean("bizExecutor", ThreadPoolTaskExecutor.class).shutdown();
            context.close();
        }
    }

    private static ThreadPoolTaskExecutor createExecutor(String threadNamePrefix, int poolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        executor.setAwaitTerminationSeconds((int) TimeUnit.SECONDS.toSeconds(1));
        return executor;
    }
}
