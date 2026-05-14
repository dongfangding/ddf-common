package com.ddf.boot.common.lock.zk.impl;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.zk.config.DistributedLockZookeeperProperties;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Value;

/**
 * 基于zookeeper实现的分布式锁$
 *
 * @author dongfang.ding
 * @since 2020/3/13 0013 16:56
 */
@Slf4j
public class ZookeeperDistributedLock implements DistributedLock {

    public static final String BEAN_NAME = "zookeeperDistributedLock";

    private final CuratorFramework client;
    private final DistributedLockZookeeperProperties distributedLockZookeeperProperties;

    public ZookeeperDistributedLock(CuratorFramework client,
            DistributedLockZookeeperProperties distributedLockZookeeperProperties) {
        this.client = client;
        this.distributedLockZookeeperProperties = distributedLockZookeeperProperties;
    }

    @Value("${spring.profiles.active:local}")
    private String env;

    /**
     * 尝试获取锁并执行业务, 与其它不同的是，这个加锁失败，不提供失败回调也不会抛出异常
     *
     * @param lockKey 锁
     * @param waitTime 加锁等待时间
     * @param timeUnit 加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则返回null
     * @param <R> 返回值泛型类型
     */
    @Override
    public <R> R tryLock(String lockKey, int waitTime, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) {
        String formatLockKey = formatLockKey(lockKey);
        InterProcessMutex lock = new InterProcessMutex(client, formatLockKey);
        boolean acquired = false;
        try {
            acquired = lock.acquire(waitTime, timeUnit);
        } catch (Exception e) {
            log.error("zk-尝试获取锁失败, thread = {}, lockKey = {}, time = {}ms", Thread.currentThread().getName(),
                    lockKey, timeUnit.toMillis(waitTime));
        }
        if (!acquired) {
            if (Objects.nonNull(failureHandler)) {
                return failureHandler.handle();
            }
            return null;
        }
        try {
            return successHandler.handle();
        } finally {
            // 释放锁逻辑
            if (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                } catch (Exception e) {
                    log.error("zk-释放锁失败, lockKey = {}", lockKey, e);
                }
            }
        }
    }


    /**
     * 指定等待时间加锁并执行业务
     *
     * @param lockKey 锁
     * @param waitTime 加锁等待时间
     * @param timeUnit 加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @param <R> 返回值泛型类型
     */
    @Override
    public <R> R lockWork(String lockKey, int waitTime, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) {
        String formatLockKey = formatLockKey(lockKey);
        InterProcessMutex lock = new InterProcessMutex(client, formatLockKey);
        boolean locked = false;
        try {
            locked = lock.acquire(waitTime, timeUnit);
        } catch (Exception e) {
            log.error("zk-加锁失败, thread = {}, lockKey = {}, time = {}ms", Thread.currentThread().getName(), lockKey,
                    timeUnit.toMillis(waitTime), e);
        }
        if (!locked) {
            if (Objects.nonNull(failureHandler)) {
                return failureHandler.handle();
            }
            return null;
        }
        try {
            return successHandler.handle();
        } finally {
            // 释放锁逻辑
            if (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                } catch (Exception e) {
                    log.error("zk-释放锁失败, lockKey = {}", lockKey, e);
                }
            }
        }
    }

    /**
     * 等待默认时间加锁并执行业务
     *
     * @param lockKey 锁
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @param <R> 返回值泛型类型
     */
    @Override
    public <R> R lockWork(String lockKey, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler) {
        return lockWork(lockKey, DistributedLock.DEFAULT_ACQUIRE_TIME, DistributedLock.DEFAULT_ACQUIRE_TIME_UNIT,
                successHandler, failureHandler);
    }

    /**
     * 上锁路径格式化
     *
     * @param lockKey lock键参数
     */
    @Override
    public String formatLockKey(String lockKey) {
        if (StringUtils.isBlank(lockKey) || !lockKey.startsWith("/")) {
            throw new IllegalStateException(" lockKey error, lockKey must start with /, lockKey=" + lockKey);
        }
        return distributedLockZookeeperProperties.getRoot() + "/" + env + "/locks" + lockKey;
    }
}
