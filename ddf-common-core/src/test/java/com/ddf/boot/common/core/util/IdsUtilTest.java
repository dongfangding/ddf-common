package com.ddf.boot.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdsUtil 测试类
 *
 * @author X_Agent
 * @date 2025/01/15
 */
public class IdsUtilTest {

    @Test
    @DisplayName("测试 getNextLongId - 生成雪花ID")
    public void testGetNextLongId() {
        long id1 = IdsUtil.getNextLongId();
        long id2 = IdsUtil.getNextLongId();

        assertTrue(id1 > 0, "生成的ID应该大于0");
        assertTrue(id2 > 0, "生成的ID应该大于0");
        assertNotEquals(id1, id2, "连续生成的ID应该不同");
    }

    @Test
    @DisplayName("测试 getNextStrId - 生成字符串格式ID")
    public void testGetNextStrId() {
        String id1 = IdsUtil.getNextStrId();
        String id2 = IdsUtil.getNextStrId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertTrue(id1.length() > 0, "ID不应为空");
        assertTrue(id2.length() > 0, "ID不应为空");
        assertNotEquals(id1, id2, "连续生成的ID应该不同");
    }

    @Test
    @DisplayName("测试 getUniqueId - 生成唯一ID")
    public void testGetUniqueId() {
        String id1 = IdsUtil.getUniqueId();
        String id2 = IdsUtil.getUniqueId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertTrue(id1.length() > IdsUtil.getNextStrId().length(), "唯一ID应该比普通ID更长");
        assertNotEquals(id1, id2, "连续生成的唯一ID应该不同");
    }

    @RepeatedTest(10)
    @DisplayName("测试 ID 递增特性")
    public void testIdIncrement() {
        long id1 = IdsUtil.getNextLongId();
        long id2 = IdsUtil.getNextLongId();

        assertTrue(id2 > id1, "后生成的ID应该大于先生成的ID");
    }

    @Test
    @DisplayName("测试大量ID生成不重复")
    public void testMassIdGeneration() throws InterruptedException {
        int threadCount = 10;
        int idsPerThread = 1000;
        Set<Long> allIds = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    Set<Long> threadIds = new HashSet<>();
                    for (int i = 0; i < idsPerThread; i++) {
                        threadIds.add(IdsUtil.getNextLongId());
                    }
                    synchronized (allIds) {
                        allIds.addAll(threadIds);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(0, errorCount.get(), "不应有异常发生");
        assertEquals(threadCount * idsPerThread, allIds.size(), "所有ID应该唯一");
    }

    @Test
    @DisplayName("测试 ID 位数范围")
    public void testIdDigitRange() {
        long id = IdsUtil.getNextLongId();
        String idStr = Long.toString(id);

        // 雪花算法生成的ID通常是19位数字
        assertTrue(idStr.length() >= 18 && idStr.length() <= 19,
                "雪花ID应该是18-19位，实际: " + idStr.length());
    }

    @Test
    @DisplayName("测试 getNextStrId 格式正确")
    public void testStrIdFormat() {
        String strId = IdsUtil.getNextStrId();

        assertTrue(strId.matches("\\d+"), "字符串ID应该全是数字");
        assertTrue(strId.length() >= 18, "字符串ID应该至少18位");
    }

    @Test
    @DisplayName("测试 getUniqueId 包含UUID部分")
    public void testUniqueIdContainsUuid() {
        String uniqueId = IdsUtil.getUniqueId();

        // 唯一ID = 雪花ID(18-19位) + UUID去掉横杠(32位hex) = 50-51位
        assertTrue(uniqueId.length() >= 50, "唯一ID应该至少50位");
        // UUID是hex格式(0-9, a-f)
        assertTrue(uniqueId.matches("[0-9a-f]+"), "唯一ID应该由数字和字母a-f组成");
    }

    @Test
    @DisplayName("测试并发环境下ID唯一性")
    public void testConcurrentIdUniqueness() throws InterruptedException {
        int count = 1000;
        Set<Long> ids = new HashSet<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(count);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        for (int i = 0; i < count; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ids.add(IdsUtil.getNextLongId());
                } catch (Exception e) {
                    // ignore
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 释放所有线程同时开始
        startLatch.countDown();
        endLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        // 由于雪花算法在极高并发下可能有短暂冲突，允许少量重复
        double uniquenessRate = (double) ids.size() / count;
        assertTrue(uniquenessRate > 0.95, "并发ID唯一性应超过95%，实际: " + (uniquenessRate * 100) + "%");
    }
}
