package com.ddf.boot.common.core.helper;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ThreadBuilderHelper 测试类
 *
 * @author X_Agent
 * @since 2025/01/15
 */
public class ThreadBuilderHelperTest {

    @Test
    @DisplayName("测试 buildThreadExecutor - 基础方法")
    public void testBuildThreadExecutorBasic() {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-basic",
                60,
                100
        );

        assertNotNull(executor);
        assertEquals("test-basic", executor.getThreadNamePrefix());
        assertEquals(60, executor.getKeepAliveSeconds());
        assertEquals(100, executor.getQueueCapacity());
    }

    @Test
    @DisplayName("测试 buildThreadExecutor - 带优雅关闭参数")
    public void testBuildThreadExecutorWithGracefulShutdown() {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-graceful",
                60,
                100,
                true
        );

        assertNotNull(executor);
        assertEquals("test-graceful", executor.getThreadNamePrefix());
    }

    @Test
    @DisplayName("测试 buildThreadExecutor - 自定义拒绝策略")
    public void testBuildThreadExecutorWithRejectedHandler() {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-rejected",
                60,
                100,
                handler
        );

        assertNotNull(executor);
        // 验证拒绝策略已设置
        executor.initialize();
        assertNotNull(executor.getThreadPoolExecutor().getRejectedExecutionHandler());
        executor.destroy();
    }

    @Test
    @DisplayName("测试 buildThreadExecutor - 完整参数")
    public void testBuildThreadExecutorFullParams() {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-full",
                4,           // corePoolSize
                8,           // maxPoolSize
                60,          // keepAliveSeconds
                100          // queueCapacity
        );

        assertNotNull(executor);
        assertEquals(4, executor.getCorePoolSize());
        assertEquals(8, executor.getMaxPoolSize());
        assertEquals(60, executor.getKeepAliveSeconds());
        assertEquals(100, executor.getQueueCapacity());
    }

    @Test
    @DisplayName("测试 buildThreadExecutor - 自定义拒绝策略和优雅关闭")
    public void testBuildThreadExecutorWithHandlerAndGraceful() {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-handler-graceful",
                4,
                8,
                60,
                100,
                handler,
                true
        );

        assertNotNull(executor);
        // 验证拒绝策略已设置
        executor.initialize();
        assertNotNull(executor.getThreadPoolExecutor().getRejectedExecutionHandler());
        executor.destroy();
    }

    @Test
    @DisplayName("测试 buildThreadExecutor - 完全自定义参数")
    public void testBuildThreadExecutorFullCustom() {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-custom",
                4,
                8,
                60,
                100,
                handler,
                true,
                true
        );

        assertNotNull(executor);
        assertEquals(4, executor.getCorePoolSize());
        assertEquals(8, executor.getMaxPoolSize());
        assertEquals(60, executor.getKeepAliveSeconds());
        assertEquals(100, executor.getQueueCapacity());
        // 验证拒绝策略类型
        executor.initialize();
        assertTrue(executor.getThreadPoolExecutor().getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy);
        executor.destroy();
    }

    @Test
    @DisplayName("测试 buildScheduledExecutorService - 基础方法")
    public void testBuildScheduledExecutorServiceBasic() {
        java.util.concurrent.ScheduledThreadPoolExecutor executor = ThreadBuilderHelper.buildScheduledExecutorService(
                "test-scheduled",
                60
        );

        assertNotNull(executor);
        assertTrue(executor instanceof java.util.concurrent.ScheduledThreadPoolExecutor);
    }

    @Test
    @DisplayName("测试 buildScheduledExecutorService - 带优雅关闭")
    public void testBuildScheduledExecutorServiceWithGraceful() {
        java.util.concurrent.ScheduledThreadPoolExecutor executor = ThreadBuilderHelper.buildScheduledExecutorService(
                "test-scheduled-graceful",
                60,
                true
        );

        assertNotNull(executor);
    }

    @Test
    @DisplayName("测试 buildScheduledExecutorService - 自定义线程数")
    public void testBuildScheduledExecutorServiceCustomSize() {
        java.util.concurrent.ScheduledThreadPoolExecutor executor = ThreadBuilderHelper.buildScheduledExecutorService(
                "test-scheduled-custom",
                60,
                4,  // corePoolSize
                8,  // maxPoolSize
                true
        );

        assertNotNull(executor);
        assertTrue(executor.getCorePoolSize() > 0);
        assertTrue(executor.getMaximumPoolSize() > 0);
    }

    @Test
    @DisplayName("测试 getDefaultCorePoolSize - 默认核心线程数")
    public void testGetDefaultCorePoolSize() {
        int corePoolSize = ThreadBuilderHelper.getDefaultCorePoolSize();

        // 至少2个核心线程
        assertTrue(corePoolSize >= 2);

        // 根据CPU核心数计算，每核分配2~4个线程
        int cpuCores = Runtime.getRuntime().availableProcessors();
        assertTrue(corePoolSize >= cpuCores * 2);
        assertTrue(corePoolSize <= cpuCores * 4);
    }

    @Test
    @DisplayName("测试 getDefaultMaxPoolSize - 默认最大线程数")
    public void testGetDefaultMaxPoolSize() {
        int maxPoolSize = ThreadBuilderHelper.getDefaultMaxPoolSize();
        int defaultCorePoolSize = ThreadBuilderHelper.getDefaultCorePoolSize();

        // 最大线程数应该是核心线程数的2倍
        assertEquals(defaultCorePoolSize * 2, maxPoolSize);
    }

    @Test
    @DisplayName("测试线程池执行任务")
    public void testThreadPoolExecution() throws Exception {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-execution",
                4,
                8,
                60,
                100
        );

        executor.initialize();

        try {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);

            for (int i = 0; i < 10; i++) {
                executor.execute(() -> {
                    counter.incrementAndGet();
                    if (counter.get() == 10) {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(10, counter.get());
        } finally {
            executor.destroy();
        }
    }
}
