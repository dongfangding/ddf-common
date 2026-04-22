package com.ddf.boot.common.core.helper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ThreadBuilderHelper 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
public class ThreadBuilderHelperTest {

    @Test
    @DisplayName("测试 buildThreadExecutor 基础参数")
    public void testBuildThreadExecutorBasic() {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor("test-basic", 60, 100);

        assertNotNull(executor);
        assertEquals("test-basic", executor.getThreadNamePrefix());
        assertEquals(60, executor.getKeepAliveSeconds());
        assertEquals(100, executor.getQueueCapacity());
    }

    @Test
    @DisplayName("测试 buildThreadExecutor 优雅关闭参数")
    public void testBuildThreadExecutorWithGracefulShutdown() {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor("test-graceful", 60, 100, true);

        assertNotNull(executor);
        assertEquals("test-graceful", executor.getThreadNamePrefix());
    }

    @Test
    @DisplayName("测试 buildThreadExecutor 自定义拒绝策略")
    public void testBuildThreadExecutorWithRejectedHandler() {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor("test-rejected", 60, 100, handler);

        executor.initialize();
        try {
            assertNotNull(executor.getThreadPoolExecutor().getRejectedExecutionHandler());
        } finally {
            executor.destroy();
        }
    }

    @Test
    @DisplayName("测试 buildThreadExecutor 完整参数")
    public void testBuildThreadExecutorFullParams() {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor("test-full", 60, 100, 4, 8);

        assertNotNull(executor);
        assertEquals(4, executor.getCorePoolSize());
        assertEquals(8, executor.getMaxPoolSize());
    }

    @Test
    @DisplayName("测试 buildThreadExecutor 拒绝策略和优雅关闭")
    public void testBuildThreadExecutorWithHandlerAndGraceful() {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-handler-graceful", 60, 100, 4, 8, handler, true
        );

        executor.initialize();
        try {
            assertEquals(4, executor.getCorePoolSize());
            assertEquals(8, executor.getMaxPoolSize());
            assertNotNull(executor.getThreadPoolExecutor().getRejectedExecutionHandler());
        } finally {
            executor.destroy();
        }
    }

    @Test
    @DisplayName("测试 buildThreadExecutor 完全自定义参数")
    public void testBuildThreadExecutorFullCustom() {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
                "test-custom", 60, 100, 4, 8, handler, true, true
        );

        executor.initialize();
        try {
            assertTrue(executor.getThreadPoolExecutor().getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy);
        } finally {
            executor.destroy();
        }
    }

    @Test
    @DisplayName("测试 buildScheduledExecutorService 基础方法")
    public void testBuildScheduledExecutorServiceBasic() {
        ScheduledThreadPoolExecutor executor = ThreadBuilderHelper.buildScheduledExecutorService("test-scheduled", 60);

        assertNotNull(executor);
        assertTrue(executor.getCorePoolSize() > 0);
    }

    @Test
    @DisplayName("测试 buildScheduledExecutorService 自定义线程数")
    public void testBuildScheduledExecutorServiceCustomSize() {
        ScheduledThreadPoolExecutor executor = ThreadBuilderHelper.buildScheduledExecutorService(
                "test-scheduled-custom", 60, true, 4, 8
        );

        assertNotNull(executor);
        assertEquals(4, executor.getCorePoolSize());
        assertTrue(executor.getMaximumPoolSize() > 0);
    }

    @Test
    @DisplayName("测试默认线程池参数范围")
    public void testDefaultPoolSizeRange() {
        int corePoolSize = ThreadBuilderHelper.getDefaultCorePoolSize();
        int maxPoolSize = ThreadBuilderHelper.getDefaultMaxPoolSize();
        int cpuCores = Runtime.getRuntime().availableProcessors();

        assertTrue(corePoolSize >= 2);
        assertTrue(corePoolSize >= cpuCores * 2);
        assertTrue(corePoolSize <= cpuCores * 4);
        assertEquals(corePoolSize * 2, maxPoolSize);
    }

    @Test
    @DisplayName("测试线程池应可执行提交任务")
    public void testThreadPoolExecution() throws Exception {
        ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor("test-execution", 60, 100, 4, 8);
        executor.initialize();

        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger counter = new AtomicInteger(0);

            for (int i = 0; i < 10; i++) {
                executor.execute(() -> {
                    if (counter.incrementAndGet() == 10) {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(10, counter.get());
        } finally {
            executor.destroy();
        }
    }
}
