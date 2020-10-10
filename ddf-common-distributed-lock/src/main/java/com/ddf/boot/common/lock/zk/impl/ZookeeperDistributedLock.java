package com.ddf.boot.common.lock.zk.impl;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.exception.LockingAcquireException;
import com.ddf.boot.common.lock.exception.LockingBusinessException;
import com.ddf.boot.common.lock.zk.config.DistributedLockZookeeperProperties;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
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
     * @param lockPath
     * @return
     */
    @SneakyThrows
    @Override
    public Boolean tryLock(String lockPath, int time, TimeUnit timeUnit, HandlerBusiness handleData) {
        String formatLockPath = formatLockPath(lockPath);
        InterProcessMutex lock = new InterProcessMutex(client, formatLockPath);
        try {
            if (!lock.acquire(time, timeUnit)) {
                log.warn("{}等待[{}{}]后未获取到锁", lockPath, time, timeUnit);
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("尝试获取锁路径[{}]时出错！", formatLockPath);
            return false;
        }

        try {
            handleData.handle();
        } catch (Exception e) {
            log.error("在锁[{}]执行业务时出错！",formatLockPath);
            throw new LockingBusinessException(e);
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                lock.release();
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 只需要一个执行成功，通过将阻塞的时间设置一个非常短的时间，保证同一个业务有一个加锁成功之后，其它服务不需要继续阻塞获取锁， 加锁失败直接返回false,
     * fixme 但这种方式显然是不准确的， 更不准的一种情况是获取了分布式锁但是还没释放之后的那台机器掉线了，由于zk对客户端感知的延迟性，再未删除节点前另外一太机器会多次内无法获取到锁，影响
     * 业务流程执行
     * @param lockPath
     * @param handleData
     * @return
     */
    @Override
    public boolean lockWorkOnce(String lockPath, HandlerBusiness handleData) {
        return tryLock(lockPath, 10, TimeUnit.MILLISECONDS, handleData);
    }

    /**
     * 加锁并执行业务
     *
     * @param lockPath   zk节点路径
     * @param time       等待获取锁的时间
     * @param timeUnit   单位
     * @param handleData 具体业务
     */
    @SneakyThrows
    @Override
    public void lockWork(String lockPath, int time, TimeUnit timeUnit, HandlerBusiness handleData) {
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
                lock.release();
            }
        }
    }

    /**
     * 加锁并执行业务- 加锁默认等待10s获取不到锁抛出异常IllegalStateException
     *
     * @param lockPath   zk节点路径
     * @param handleData 具体业务
     * @return
     */
    @Override
    public void lockWork(String lockPath, HandlerBusiness handleData) {
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
