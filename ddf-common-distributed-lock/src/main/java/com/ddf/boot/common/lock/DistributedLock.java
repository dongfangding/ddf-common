package com.ddf.boot.common.lock;

import com.ddf.boot.common.lock.exception.LockingAcquireException;
import com.ddf.boot.common.lock.exception.LockingReleaseException;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口$
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
 * @date 2020/3/13 0013 16:30
 */
public interface DistributedLock {

    /**
     * 默认等待时间 10s
     */
    Integer DEFAULT_ACQUIRE_TIME = 10;
    /**
     * 默认等待时间 单位
     */
    TimeUnit DEFAULT_ACQUIRE_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 尝试获取锁
     * @param lockPath
     * @return
     */
    boolean tryLock(String lockPath) ;

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
    void lockWork(String lockPath, int time, TimeUnit timeUnit, HandlerBusiness handleData)
            throws LockingAcquireException, LockingReleaseException;

    /**
     * 加锁并执行业务- 加锁默认等待10s获取不到锁抛出异常IllegalStateException
     *
     * @param lockPath   zk节点路径
     * @param handleData 具体业务
     * @return
     * @throws LockingAcquireException  获取锁异常
     * @throws LockingReleaseException  释放锁异常
     */
    void lockWork(String lockPath, HandlerBusiness handleData) throws LockingAcquireException, LockingReleaseException;

    /**
     * 上锁路径格式化
     *
     * @param lockPath
     * @return
     */
    String formatLockPath(String lockPath);



    @FunctionalInterface
    interface HandlerBusiness {
        /**
         * 执行业务
         */
        void handle();
    }
}
