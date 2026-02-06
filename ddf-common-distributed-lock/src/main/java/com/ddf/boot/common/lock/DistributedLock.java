package com.ddf.boot.common.lock;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 * 分布式锁接口
 *
 * @author dongfang.ding
 * @since 2020/3/13 0013 16:30
 */
public interface DistributedLock {

    /**
     * 针对多个路径进行格式化，用以符合zk node格式
     * zk分布式锁时使用
     *
     * @param path
     * @return
     */
    static String formatPath(String... path) {
        return StringUtils.join(path, "/");
    }

    /**
     * 默认等待时间 10s
     */
    Integer DEFAULT_ACQUIRE_TIME = 1000;
    /**
     * 默认等待时间 单位
     */
    TimeUnit DEFAULT_ACQUIRE_TIME_UNIT = TimeUnit.MILLISECONDS;

    /**
     * 尝试获取锁并执行业务, 非阻塞的获取到，最大等待时间waitTime之后还获取不到锁的话会返回失败， 如果想要获取不到立马失败， waitTime可以指定为0
     *
     * @param lockKey        锁
     * @param waitTime       加锁等待时间
     * @param timeUnit       加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则返回null
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R tryLock(String lockKey, int waitTime, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler);

    /**
     * 只立马尝试一次获取锁，获取不到就返回失败， redis的实现，获取到使用看门狗续期
     *
     * @param lockKey        锁
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则返回null
     * @param <R>
     * @return
     * @throws Exception
     */
    default <R> R tryLockOnce(String lockKey, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler) {
        return tryLock(lockKey, 0, TimeUnit.SECONDS, successHandler, failureHandler);
    }


    /**
     * 指定加锁时间并执行业务， leaseTime是获取到锁的最大持有时间，如果获取不到锁，会一直尝试获取
     *
     * @param lockKey        锁
     * @param leaseTime      最大持有锁时间
     * @param timeUnit       加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R lockWork(String lockKey, int leaseTime, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler);

    /**
     * 持有默认时间加锁并执行业务，如果获取不到锁，会一直尝试获取，这个如果是redis实现的话，即使用看门狗来续期时间
     *
     * @param lockKey        锁
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R lockWork(String lockKey, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler);

    /**
     * 上锁路径格式化， zk专用
     *
     * @param lockKey
     * @return
     */
    default String formatLockKey(String lockKey) {
        return lockKey;
    }

    /**
     * 执行业务方法
     */
    @FunctionalInterface
    interface BusinessHandler<R> {

        /**
         * 执行业务
         *
         * @return
         * @throws
         */
        R handle();
    }
}
