package com.ddf.boot.common.lock;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

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
    Integer DEFAULT_ACQUIRE_TIME = 10;
    /**
     * 默认等待时间 单位
     */
    TimeUnit DEFAULT_ACQUIRE_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 尝试获取锁并执行业务
     *
     * @param lockKey
     * @param time
     * @param timeUnit
     * @param handleData
     * @return
     */
    Boolean tryLock(String lockKey, int time, TimeUnit timeUnit, SuccessHandler handleData);


    /**
     * 加锁并执行业务
     *
     * @param lockKey   zk节点路径
     * @param time       等待获取锁的时间
     * @param timeUnit   单位
     * @param handleData 具体业务
     */
    void lockWork(String lockKey, int time, TimeUnit timeUnit, SuccessHandler handleData);

    /**
     * 加锁并执行业务- 加锁默认等待10s获取不到锁抛出异常IllegalStateException
     *
     * @param lockKey   zk节点路径
     * @param handleData 具体业务
     * @return
     */
    void lockWork(String lockKey, SuccessHandler handleData);

    /**
     * 上锁路径格式化
     *
     * @param lockKey
     * @return
     */
    String formatLockKey(String lockKey);

    /**
     * 加锁成功执行业务
     */
    @FunctionalInterface
    interface SuccessHandler {
        /**
         * 执行业务
         */
        void handle() throws Exception;
    }
}
