package com.ddf.boot.common.core.helper;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * 创建线程
 *
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
 * @date 2019/12/11 0011 17:52
 */
public class ThreadBuilderHelper {

    /**
     * 构建线程池参数, 默认拒绝策略是将请求打回给调用线程使用
     *
     * @param prefix           线程池名称前缀
     * @param keepAliveSeconds 保持空闲时间
     * @param queueCapacity    队列大小
     * @return
     */
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity, Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2 + 1, new ThreadPoolExecutor.CallerRunsPolicy());
    }


    /**
     * 构建线程池参数, 默认拒绝策略是将请求打回给调用线程使用
     *
     * @param prefix                   线程池名称前缀
     * @param keepAliveSeconds         保持空闲时间
     * @param queueCapacity            队列大小
     * @param corePoolSize             核心线程池大小
     * @param maxPoolSize              最大线程池大小
     * @return org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
     * @author dongfang.ding
     * @date 2019/12/11 0011 17:59
     **/
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity
            , int corePoolSize, int maxPoolSize) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity, corePoolSize, maxPoolSize, new ThreadPoolExecutor.CallerRunsPolicy());
    }


    /**
     * 构建线程池参数
     *
     * @param prefix           线程池名称前缀
     * @param keepAliveSeconds 保持空闲时间
     * @param queueCapacity    队列大小
     * @return
     */
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity
            , RejectedExecutionHandler rejectedExecutionHandler) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity, Runtime.getRuntime().availableProcessors() + 1,
                Runtime.getRuntime().availableProcessors() * 2, rejectedExecutionHandler);
    }

    /**
     * 构建线程池参数
     *
     * @param prefix                   线程池名称前缀
     * @param keepAliveSeconds         保持空闲时间
     * @param queueCapacity            队列大小
     * @param corePoolSize             核心线程池大小
     * @param maxPoolSize              最大线程池大小
     * @param rejectedExecutionHandler 队列满之后的处理策略
     * @return org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
     * @author dongfang.ding
     * @date 2019/12/11 0011 17:59
     **/
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity
            , int corePoolSize, int maxPoolSize, RejectedExecutionHandler rejectedExecutionHandler) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix(prefix);
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
        return threadPoolTaskExecutor;
    }



    /**
     * 构建定时任务线程池
     * @param prefix
     * @param keepAliveSeconds
     * @return
     */
    public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int keepAliveSeconds) {
        return buildScheduledExecutorService(prefix, Runtime.getRuntime().availableProcessors() + 1,
                Runtime.getRuntime().availableProcessors() * 2, keepAliveSeconds);
    }

    /**
     * 构建定时任务线程池
     * @param prefix
     * @param corePoolSize
     * @param maxPoolSize
     * @return
     */
    public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int corePoolSize, int maxPoolSize
            , int keepAliveSeconds) {
        ThreadFactory namedThreadFactory = new CustomizableThreadFactory(prefix);
        ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize, namedThreadFactory);
        scheduledExecutorService.setMaximumPoolSize(maxPoolSize);
        scheduledExecutorService.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
        return scheduledExecutorService;
    }
}
