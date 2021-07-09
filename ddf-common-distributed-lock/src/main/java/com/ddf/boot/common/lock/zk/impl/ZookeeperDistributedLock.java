package com.ddf.boot.common.lock.zk.impl;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.exception.LockingAcquireException;
import com.ddf.boot.common.lock.exception.LockingBusinessException;
import com.ddf.boot.common.lock.zk.config.DistributedLockZookeeperProperties;
import com.google.common.base.Strings;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
@Component
@Slf4j
public class ZookeeperDistributedLock implements DistributedLock {

    @Autowired
    private CuratorFramework client;
    @Value("${spring.profiles.active:local}")
    private String env;
    @Autowired
    private DistributedLockZookeeperProperties distributedLockZookeeperProperties;


    /**
     * 尝试获取锁
     *
     * @param lockKey
     * @return
     */
    @SneakyThrows
    @Override
    public Boolean tryLock(String lockKey, int time, TimeUnit timeUnit, SuccessHandler handleData) {
        String formatLockKey = formatLockKey(lockKey);
        InterProcessMutex lock = new InterProcessMutex(client, formatLockKey);
        try {
            if (!lock.acquire(time, timeUnit)) {
                log.warn("{}等待[{}{}]后未获取到锁", lockKey, time, timeUnit);
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("尝试获取锁路径[{}]时出错！", formatLockKey);
            return false;
        }

        try {
            handleData.handle();
        } catch (Exception e) {
            log.error("在锁[{}]执行业务时出错！", formatLockKey);
            throw new LockingBusinessException(e);
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                lock.release();
            }
        }
        return Boolean.TRUE;
    }


    /**
     * 加锁并执行业务
     *
     * @param lockKey   zk节点路径
     * @param time       等待获取锁的时间
     * @param timeUnit   单位
     * @param handleData 具体业务
     */
    @SneakyThrows
    @Override
    public void lockWork(String lockKey, int time, TimeUnit timeUnit, SuccessHandler handleData) {
        String formatLockKey = formatLockKey(lockKey);
        InterProcessMutex lock;
        try {
            lock = new InterProcessMutex(client, formatLockKey);
            if (!lock.acquire(time, timeUnit)) {
                throw new IllegalStateException(lock + " could not acquire the lock");
            }
        } catch (Exception e) {
            log.error("尝试获取锁路径[{}]时出错！", formatLockKey);
            throw new LockingAcquireException(e);
        }

        try {
            handleData.handle();
        } catch (Exception e) {
            log.error("在锁[{}]执行业务时出错！", formatLockKey);
            throw new LockingBusinessException(e);
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                lock.release();
            }
        }
    }

    /**
     * 加锁并执行业务- 加锁默认等待10s获取不到锁抛出异常IllegalStateException
     *
     * @param lockKey   zk节点路径
     * @param handleData 具体业务
     * @return
     */
    @Override
    public void lockWork(String lockKey, SuccessHandler handleData) {
        lockWork(lockKey, DistributedLock.DEFAULT_ACQUIRE_TIME, DistributedLock.DEFAULT_ACQUIRE_TIME_UNIT, handleData);
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
