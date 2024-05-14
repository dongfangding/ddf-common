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
 * <p>基于redisson实现分布式锁</p >
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
     * 尝试获取锁并执行业务, 这个获取锁是非阻塞的，意味着获取锁时如果获取不到会直接失败，但是这个也是有等待时间的，如果time > 0的话，
     * 锁里面的业务代码执行时间小于指定的等待时间，依然是可以获取到锁的
     *
     * @param lockKey        锁
     * @param waitTime       加锁等待时间
     * @param timeUnit       加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则返回null
     * @return
     * @throws Exception
     */
    @Override
    public <R> R tryLock(String lockKey, int waitTime, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) throws Exception {
        RLock lock = redissonClient.getLock(lockKey);
        // tryLock默认leaseTime是-1， 有看门狗续期机制， 也可以调用指定的leaseTime的
        // boolean locked = lock.tryLock(waitTime, 10, timeUnit);
        boolean locked = lock.tryLock(waitTime, timeUnit);
        if (!locked) {
            log.warn("redisson-尝试获取锁失败, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
            if (Objects.nonNull(failureHandler)) {
                log.warn(
                        "redisson-执行加锁失败回调, thread = {}, lockKey = {}", Thread.currentThread().getName(),
                        lockKey
                );
                return failureHandler.handle();
            }
            return null;
        }
        try {
            return successHandler.handle();
        } catch (Exception e) {
            log.error(
                    "redisson-加锁执行业务失败, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey,
                    e
            );
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * redisson加锁的实现是 如有必要，如果锁被持有那么就会一直阻塞一直等到拿到锁.
     * leaseTime是拿到锁之后多久释放锁，使用这个特性看门狗会失效
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
            } catch (Exception e) {
                log.error(
                        "redisson-加锁执行业务失败, thread = {}, lockKey = {}", Thread.currentThread().getName(),
                        lockKey, e
                );
                throw e;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        log.warn("redisson-尝试获取锁失败, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
        if (Objects.nonNull(failureHandler)) {
            log.warn("redisson-执行加锁失败回调, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
            return failureHandler.handle();
        }
        throw new LockingAcquireException(lockKey);
    }

    /**
     * lock时不指定leaseTime使用看门狗特性来执行加锁业务
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
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        if (lock.isHeldByCurrentThread()) {
            try {
                return successHandler.handle();
            } catch (Exception e) {
                log.error(
                        "redisson看门狗-加锁执行业务失败, thread = {}, lockKey = {}", Thread.currentThread().getName(),
                        lockKey, e
                );
                throw e;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        log.warn("redisson看门狗-尝试获取锁失败, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
        if (Objects.nonNull(failureHandler)) {
            log.warn(
                    "redisson看门狗-执行加锁失败回调, thread = {}, lockKey = {}", Thread.currentThread().getName(),
                    lockKey
            );
            return failureHandler.handle();
        }
        throw new LockingAcquireException(lockKey);
    }
}
