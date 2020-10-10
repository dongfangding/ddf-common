package com.ddf.boot.common.lock;

import org.apache.commons.lang3.StringUtils;

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
     * 针对多个路径进行格式化，用以符合zk node格式
     * @param path
     * @return
     */
    static String formatPath(String... path) {
        return StringUtils.join(path, "/");
    }

    /**
     * 默认等待时间 10s
     */
    Integer DEFAULT_ACQUIRE_TIME = 10;
    /**
     * 默认等待时间 单位
     */
    TimeUnit DEFAULT_ACQUIRE_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 尝试获取锁并执行业务
     * @param lockPath
     * @param time
     * @param timeUnit
     * @param handleData
     * @return
     */
    Boolean tryLock(String lockPath, int time, TimeUnit timeUnit, HandlerBusiness handleData);

    /**
     * 只需要一个执行成功，通过将阻塞的时间设置一个非常短的时间，保证同一个业务有一个加锁成功之后，其它服务不需要继续阻塞获取锁， 加锁失败直接返回false,
     * @param lockPath
     * @param handleData
     * @return
     */
    boolean lockWorkOnce(String lockPath, HandlerBusiness handleData);

    /**
     * 加锁并执行业务
     *
     * @param lockPath   zk节点路径
     * @param time       等待获取锁的时间
     * @param timeUnit   单位
     * @param handleData 具体业务
     */
    void lockWork(String lockPath, int time, TimeUnit timeUnit, HandlerBusiness handleData);

    /**
     * 加锁并执行业务- 加锁默认等待10s获取不到锁抛出异常IllegalStateException
     *
     * @param lockPath   zk节点路径
     * @param handleData 具体业务
     * @return
     */
    void lockWork(String lockPath, HandlerBusiness handleData);

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
        void handle() throws Exception;
    }
}
