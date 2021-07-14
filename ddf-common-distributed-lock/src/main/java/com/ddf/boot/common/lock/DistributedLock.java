package com.ddf.boot.common.lock;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 * 分布式锁接口
 *
 * @author dongfang.ding
 * @date 2020/3/13 0013 16:30
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
     * 尝试获取锁并执行业务, 与其它不同的是，这个加锁失败，不提供失败回调也不会抛出异常
     *
     * @param lockKey        锁
     * @param time           加锁等待时间
     * @param timeUnit       加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则返回null
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R tryLock(String lockKey, int time, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) throws Exception;


    /**
     * 指定等待时间加锁并执行业务
     *
     * @param lockKey        锁
     * @param time           加锁等待时间
     * @param timeUnit       加锁等待时间单位
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R lockWork(String lockKey, int time, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) throws Exception;

    /**
     * 等待默认时间加锁并执行业务
     *
     * @param lockKey        锁
     * @param successHandler 加锁成功回调
     * @param failureHandler 加锁失败回调， 如果未提供则抛出加锁失败异常
     * @param <R>
     * @return
     * @throws Exception
     */
    <R> R lockWork(String lockKey, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler)
            throws Exception;

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
         * @throws Exception
         */
        R handle() throws Exception;
    }
}
