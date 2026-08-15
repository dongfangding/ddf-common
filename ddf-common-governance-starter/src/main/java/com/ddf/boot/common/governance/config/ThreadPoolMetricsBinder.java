package com.ddf.boot.common.governance.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

/**
 * Binds Micrometer metrics for supported thread pool beans after the Spring context is ready.
 *
 * @author dongfang.ding
 * @since 2026/3/23
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolMetricsBinder implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    private final MeterRegistry meterRegistry;

    private final GovernanceProperties governanceProperties;

    @Override
    public void afterSingletonsInstantiated() {
        GovernanceProperties.ThreadPool properties = governanceProperties.getObservability().getThreadPool();
        if (!governanceProperties.getObservability().isEnabled() || !properties.isEnabled()) {
            return;
        }

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int boundCount = 0;
        for (String beanName : beanNames) {
            if (!shouldBind(beanName, properties)) {
                continue;
            }
            if (!applicationContext.isSingleton(beanName)) {
                continue;
            }
            Object bean = applicationContext.getBean(beanName);
            ExecutorService executorService = resolveExecutorService(bean);
            if (executorService == null) {
                continue;
            }
            bindExecutor(beanName, bean, executorService, properties.getMetricName());
            boundCount++;
        }
        log.info("Thread pool metrics binder completed, bound {} executor beans.", boundCount);
    }

    private boolean shouldBind(String beanName, GovernanceProperties.ThreadPool properties) {
        if (matches(beanName, properties.getExcludeBeanNamePatterns())) {
            return false;
        }
        return properties.isScanAll() || matches(beanName, properties.getIncludeBeanNamePatterns());
    }

    private boolean matches(String beanName, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (StringUtils.hasText(pattern) && PatternMatchUtils.simpleMatch(pattern, beanName)) {
                return true;
            }
        }
        return false;
    }

    private ExecutorService resolveExecutorService(Object bean) {
        if (bean instanceof ThreadPoolTaskExecutor threadPoolTaskExecutor) {
            return threadPoolTaskExecutor.getThreadPoolExecutor();
        }
        if (bean instanceof ThreadPoolTaskScheduler threadPoolTaskScheduler) {
            return threadPoolTaskScheduler.getScheduledThreadPoolExecutor();
        }
        if (bean instanceof ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
            return scheduledThreadPoolExecutor;
        }
        if (bean instanceof ThreadPoolExecutor threadPoolExecutor) {
            return threadPoolExecutor;
        }
        if (bean instanceof ExecutorService executorService) {
            return executorService;
        }
        return null;
    }

    private void bindExecutor(String beanName, Object bean, ExecutorService executorService, String metricName) {
        String sanitizedMetricName = StringUtils.hasText(metricName) ? metricName : "custom.thread.pool";
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("bean", beanName));

        String threadNamePrefix = resolveThreadNamePrefix(bean);
        if (StringUtils.hasText(threadNamePrefix)) {
            tags.add(Tag.of("threadPrefix", threadNamePrefix));
        }

        String executorType = executorService.getClass().getSimpleName();
        if (StringUtils.hasText(executorType)) {
            tags.add(Tag.of("executorType", executorType));
        }

        try {
            ExecutorServiceMetrics.monitor(meterRegistry, executorService, sanitizedMetricName, tags);
            log.debug("Bound thread pool metrics for bean [{}] with meter name [{}].", beanName, sanitizedMetricName);
        } catch (RuntimeException ex) {
            log.warn("Failed to bind thread pool metrics for bean [{}].", beanName, ex);
        }
    }

    private String resolveThreadNamePrefix(Object bean) {
        if (bean instanceof ThreadPoolTaskExecutor threadPoolTaskExecutor) {
            return threadPoolTaskExecutor.getThreadNamePrefix();
        }
        if (bean instanceof ThreadPoolTaskScheduler threadPoolTaskScheduler) {
            return threadPoolTaskScheduler.getThreadNamePrefix();
        }
        return null;
    }
}
