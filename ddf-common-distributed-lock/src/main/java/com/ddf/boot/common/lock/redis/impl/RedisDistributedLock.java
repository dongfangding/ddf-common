package com.ddf.boot.common.lock.redis.impl;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.redis.config.DistributedLockRedisProperties;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * <p>基于redisson实现分布式锁</p >
 * <p>
 * https://redisson.pro/docs/data-and-services/locks-and-synchronizers/#lock
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/07/13 20:07
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
     * 尝试获取锁并执行业务, 这个获取锁在指定的 waitTime 内是阻塞等待的。
     * 如果在 waitTime 时间内未获取到锁，则返回失败。
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
            BusinessHandler<R> failureHandler) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            // tryLock 默认leaseTime是-1， 有看门狗续期机制，只有在指定了 leaseTime (第三个参数) 时才会禁用看门狗。
            // 此处未指定 leaseTime，默认使用看门狗机制。
            // tryLock 会抛出 InterruptedException，表示等待锁的过程中线程被中断
            locked = lock.tryLock(waitTime, timeUnit);
        } catch (InterruptedException e) {
            // 恢复中断状态，供后续代码或框架判断线程状态
            Thread
                    .currentThread()
                    .interrupt();
            log.warn("redisson-获取锁时线程被中断, lockKey = {}", lockKey);
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
            // 确保只由持有锁的线程释放锁，防止锁超时后被误释放
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * redisson加锁的实现是 如有必要，如果锁被持有那么就会一直阻塞一直等到拿到锁.
     * leaseTime是拿到锁之后多久释放锁，使用这个特性看门狗会失效
     * 注意：由于 lock.lock() 是阻塞的，此方法除非抛出异常，否则必然会执行 successHandler。
     *
     * @param lockKey        锁
     * @param leaseTime      锁获取到之后多久释放锁
     * @param timeUnit       锁获取到之后多久释放锁
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调（注：在完全阻塞模式下，此参数基本无效，除非 lock 抛出异常）
     * @return
     * @throws Exception
     */
    @Override
    public <R> R lockWork(String lockKey, int leaseTime, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 该方法会一直阻塞直到获取锁成功或发生异常
            lock.lock(leaseTime, timeUnit);
        } catch (Exception e) {
            log.error(
                    "redisson-阻塞加锁发生异常, thread = {}, lockKey = {}", Thread
                            .currentThread()
                            .getName(), lockKey, e
            );
            if (Objects.nonNull(failureHandler)) {
                return failureHandler.handle();
            }
            return null;
        }

        try {
            return successHandler.handle();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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
    public <R> R lockWork(String lockKey, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 默认阻塞式获取锁，并启用看门狗续期
            lock.lock();
        } catch (Exception e) {
            log.error("redisson看门狗-阻塞加锁发生异常, thread = {}, lockKey = {}", Thread
                    .currentThread()
                    .getName(), lockKey, e
            );
            if (Objects.nonNull(failureHandler)) {
                return failureHandler.handle();
            }
        }
        try {
            return successHandler.handle();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
