package com.ddf.boot.common.lock.redis.impl;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.exception.LockingAcquireException;
import com.ddf.boot.common.lock.redis.config.DistributedLockRedisProperties;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/13 20:07
 */
@Slf4j
public class RedisDistributedLock implements DistributedLock {

    public static final String BEAN_NAME = "redisDistributedLock";

    /**
     * redisson框架
     */
    private final RedissonClient redissonClient;

    /**
     * redis锁属性类
     */
    private final DistributedLockRedisProperties distributedLockRedisProperties;

    public RedisDistributedLock(RedissonClient redissonClient, DistributedLockRedisProperties properties) {
        this.redissonClient = redissonClient;
        this.distributedLockRedisProperties = properties;
    }

    /**
     * 尝试获取锁并执行业务, 与其它不同的是，这个加锁失败，不提供失败回调也不会抛出异常
     *
     * @param lockKey        锁
     * @param time           加锁等待时间
     * @param timeUnit       加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则返回null
     * @return
     * @throws Exception
     */
    @Override
    public <R> R tryLock(String lockKey, int time, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) throws Exception {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock(time, timeUnit);
        if (!locked) {
            log.warn(
                    "redisson-尝试获取锁失败, thread = {}, lockKey = {}", Thread.currentThread()
                            .getName(), lockKey);
            if (Objects.nonNull(failureHandler)) {
                log.info(
                        "redisson-执行加锁失败回调, thread = {}, lockKey = {}", Thread.currentThread()
                                .getName(), lockKey);
                return failureHandler.handle();
            }
            return null;
        }
        try {
            return successHandler.handle();
        } catch (Exception e) {
            log.error(
                    "redisson-加锁执行业务失败, thread = {}, lockKey = {}", Thread.currentThread()
                            .getName(), lockKey);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    /**
     * redisson加锁的实现是 如有必要，如果锁被持有那么就会一直阻塞一直等到拿到锁
     * 所以这个leaseTime这个时间不是等待获取锁的最大时间，而是拿到锁之后最少多久会释放锁，
     * 当然也不是到了之后一定会释放，最终释放锁的实现还要依赖于看门狗
     *
     * @param lockKey        锁
     * @param leaseTime      锁获取到之后多久释放锁
     * @param timeUnit       锁获取到之后多久释放锁
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @return
     * @throws Exception
     */
    @Override
    public <R> R lockWork(String lockKey, int leaseTime, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) throws Exception {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, timeUnit);
        if (lock.isHeldByCurrentThread()) {
            try {
                return successHandler.handle();
            } finally {
                lock.unlock();
            }
        }
        log.warn(
                "redisson-尝试获取锁失败, thread = {}, lockKey = {}", Thread.currentThread()
                        .getName(), lockKey);
        if (Objects.nonNull(failureHandler)) {
            log.warn(
                    "redisson-执行加锁失败回调, thread = {}, lockKey = {}", Thread.currentThread()
                            .getName(), lockKey);
            return failureHandler.handle();
        }
        throw new LockingAcquireException(lockKey);
    }

    /**
     * 等待默认时间加锁并执行业务
     *
     * @param lockKey        锁
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @return
     * @throws Exception
     */
    @Override
    public <R> R lockWork(String lockKey, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler)
            throws Exception {
        return lockWork(
                lockKey, distributedLockRedisProperties.getDefaultLockTimeMillions(), TimeUnit.MILLISECONDS,
                successHandler, failureHandler
        );
    }
}
