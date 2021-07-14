package com.ddf.boot.common.lock.zk.impl;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.exception.LockingAcquireException;
import com.ddf.boot.common.lock.zk.config.DistributedLockZookeeperProperties;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Value;

/**
 * 基于zookeeper实现的分布式锁$
 * <p>
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2020/3/13 0013 16:56
 */
@Slf4j
public class ZookeeperDistributedLock implements DistributedLock {

    public static final String BEAN_NAME = "zookeeperDistributedLock";

    private final CuratorFramework client;
    private final DistributedLockZookeeperProperties distributedLockZookeeperProperties;

    public ZookeeperDistributedLock(CuratorFramework client, DistributedLockZookeeperProperties distributedLockZookeeperProperties) {
        this.client = client;
        this.distributedLockZookeeperProperties = distributedLockZookeeperProperties;
    }
    @Value("${spring.profiles.active:local}")
    private String env;

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
    @Override
    public <R> R tryLock(String lockKey, int time, TimeUnit timeUnit, BusinessHandler<R> successHandler,
            BusinessHandler<R> failureHandler) throws Exception {
        String formatLockKey = formatLockKey(lockKey);
        InterProcessMutex lock = new InterProcessMutex(client, formatLockKey);
        if (!lock.acquire(time, timeUnit)) {
            log.warn("zk-尝试获取锁失败, thread = {}, lockKey = {}, time = {}ms", Thread.currentThread().getName(), lockKey, timeUnit.toMillis(time));
            if (Objects.nonNull(failureHandler)) {
                log.info("zk-执行加锁失败回调, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
                return failureHandler.handle();
            }
            return null;
        }
        try {
            return successHandler.handle();
        } catch (Exception e) {
            log.warn("zk-加锁执行业务失败, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
            throw e;
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                lock.release();
            }
        }
    }


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
    @Override
    public <R> R lockWork(String lockKey, int time, TimeUnit timeUnit, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler) throws Exception {
        String formatLockKey = formatLockKey(lockKey);
        InterProcessMutex lock = new InterProcessMutex(client, formatLockKey);;
        if (!lock.acquire(time, timeUnit)) {
            log.warn("zk-加锁失败, thread = {}, lockKey = {}, time = {}ms", Thread.currentThread().getName(), lockKey, timeUnit.toMillis(time));
            if (Objects.nonNull(failureHandler)) {
                log.info("zk-执行加锁失败回调, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
                return failureHandler.handle();
            }
            throw new LockingAcquireException(lockKey);
        }
        try {
            return successHandler.handle();
        } catch (Exception e) {
            log.error("zk-加锁执行业务失败, thread = {}, lockKey = {}", Thread.currentThread().getName(), lockKey);
            throw e;
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                lock.release();
            }
        }
    }

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
    @Override
    public <R> R lockWork(String lockKey, BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler) throws Exception {
        return lockWork(lockKey, DistributedLock.DEFAULT_ACQUIRE_TIME, DistributedLock.DEFAULT_ACQUIRE_TIME_UNIT, successHandler, failureHandler);
    }

    /**
     * 上锁路径格式化
     *
     * @param lockKey
     * @return
     */
    @Override
    public String formatLockKey(String lockKey) {
        if (Strings.isNullOrEmpty(lockKey) || !lockKey.startsWith("/")) {
            throw new IllegalStateException(" lockKey error, lockKey must start with /, lockKey=" + lockKey);
        }
        return distributedLockZookeeperProperties.getRoot() + "/" + env + "/locks" + lockKey;
    }
}
