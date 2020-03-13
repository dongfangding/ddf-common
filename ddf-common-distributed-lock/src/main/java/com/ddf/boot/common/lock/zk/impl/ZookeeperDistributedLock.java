package com.ddf.boot.common.lock.zk.impl;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.exception.LockingAcquireException;
import com.ddf.boot.common.lock.exception.LockingBusinessException;
import com.ddf.boot.common.lock.exception.LockingReleaseException;
import com.ddf.boot.common.lock.zk.config.DistributedLockZookeeperProperties;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
     * fixme 如果不返回当前锁对象，调用方如何释放锁呢？
     * @param lockPath
     * @return
     */
    @Override
    public boolean tryLock(String lockPath) {
        String formatLockPath = formatLockPath(lockPath);
        InterProcessMutex lock = new InterProcessMutex(client, formatLockPath);
        try {
            return lock.acquire(DistributedLock.DEFAULT_ACQUIRE_TIME, DistributedLock.DEFAULT_ACQUIRE_TIME_UNIT);
        } catch (Exception e) {
            log.error("尝试获取锁路径[{}]时出错！", formatLockPath);
            return false;
        }
    }

    /**
     * 加锁并执行业务
     *
     * @param lockPath   zk节点路径
     * @param time       等待获取锁的时间
     * @param timeUnit   单位
     * @param handleData 具体业务
     * @throws LockingAcquireException  获取锁异常
     * @throws LockingReleaseException  释放锁异常
     */
    @Override
    public void lockWork(String lockPath, int time, TimeUnit timeUnit, HandlerBusiness handleData)
            throws LockingAcquireException, LockingReleaseException {
        String formatLockPath = formatLockPath(lockPath);
        InterProcessMutex lock;
        try {
            lock = new InterProcessMutex(client, formatLockPath);
            if (!lock.acquire(time, timeUnit)) {
                throw new IllegalStateException(lock + " could not acquire the lock");
            }
        } catch (Exception e) {
            log.error("尝试获取锁路径[{}]时出错！", formatLockPath);
            throw new LockingAcquireException(e);
        }

        try {
            handleData.handle();
        } catch (Exception e) {
            log.error("在锁[{}]执行业务时出错！",formatLockPath);
            throw new LockingBusinessException(e);
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                } catch (Exception e) {
                    log.error("释放锁[{}]异常", formatLockPath);
                    throw new LockingReleaseException(e);
                }
            }
        }
    }

    /**
     * 加锁并执行业务- 加锁默认等待10s获取不到锁抛出异常IllegalStateException
     *
     * @param lockPath   zk节点路径
     * @param handleData 具体业务
     * @return
     * @throws LockingAcquireException  获取锁异常
     * @throws LockingReleaseException  释放锁异常
     */
    @Override
    public void lockWork(String lockPath, HandlerBusiness handleData) throws LockingAcquireException
            , LockingReleaseException {
        lockWork(lockPath, DistributedLock.DEFAULT_ACQUIRE_TIME, DistributedLock.DEFAULT_ACQUIRE_TIME_UNIT, handleData);
    }

    /**
     * 上锁路径格式化
     *
     * @param lockPath
     * @return
     */
    @Override
    public String formatLockPath(String lockPath) {
        if (Strings.isNullOrEmpty(lockPath) || !lockPath.startsWith("/")) {
            throw new IllegalStateException(" lockPath error, lockPath must start with /, lockPath=" + lockPath);
        }
        return distributedLockZookeeperProperties.getRoot()
                + "/" + env
                +"/locks"
                + lockPath;
    }
}
