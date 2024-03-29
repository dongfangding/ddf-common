package com.ddf.boot.common.core.helper;

import com.ddf.boot.common.core.gracefulshutdown.ExecutorServiceGracefulShutdownDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 创建线程帮助类
 *
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2019/12/11 0011 17:52
 */
public class ThreadBuilderHelper {

    /**
     * 这个是给{@link ExecutorServiceGracefulShutdownDefinition#onApplicationEvent(org.springframework.context.event.ContextClosedEvent)}用的
     * 是为了方便通过该类定义线程池的地方不用再手动调用{@link ExecutorServiceGracefulShutdownDefinition#registryExecutor(ThreadPoolExecutor)}
     */
    private static final List<ExecutorService> POOLS = Collections.synchronizedList(new ArrayList<>(20));
    /**
     * 主要是ThreadPoolTaskExecutor类的ExecutorService属性在bean未创建完成是没有值的，这里只能先存原始对象，用的时候因为
     * 晚于初始化，所以用的时候再获取就没有问题了
     */
    private static final List<ThreadPoolTaskExecutor> THREAD_POOL_TASK_EXECUTOR = Collections.synchronizedList(new ArrayList<>(20));

    /**
     * 返回通过该帮助类添加的线程池
     *
     * @return
     */
    public static List<ExecutorService> getPools() {
        POOLS.addAll(THREAD_POOL_TASK_EXECUTOR.stream().map(ThreadPoolTaskExecutor::getThreadPoolExecutor).collect(Collectors.toList()));
        return POOLS;
    }

    /**
     * 构建线程池参数, 默认拒绝策略是将请求打回给调用线程使用
     *
     * @param prefix           线程池名称前缀
     * @param keepAliveSeconds 保持空闲时间
     * @param queueCapacity    队列大小
     * @return
     */
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity,
                Runtime.getRuntime().availableProcessors() + 1, Runtime.getRuntime().availableProcessors() * 2,
                new ThreadPoolExecutor.CallerRunsPolicy(), true
        );
    }

    /**
     * 构建线程池参数, 默认拒绝策略是将请求打回给调用线程使用
     *
     * @param prefix           线程池名称前缀
     * @param keepAliveSeconds 保持空闲时间
     * @param queueCapacity    队列大小
     * @param gracefulShutdown 是否优雅关闭线程池
     * @return
     */
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity, boolean gracefulShutdown) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity,
                Runtime.getRuntime().availableProcessors() + 1, Runtime.getRuntime().availableProcessors() * 2,
                new ThreadPoolExecutor.CallerRunsPolicy(), gracefulShutdown
        );
    }


    /**
     * 构建线程池参数, 默认拒绝策略是将请求打回给调用线程使用
     *
     * @param prefix           线程池名称前缀
     * @param keepAliveSeconds 保持空闲时间
     * @param queueCapacity    队列大小
     * @param corePoolSize     核心线程池大小
     * @param maxPoolSize      最大线程池大小
     * @param gracefulShutdown 是否优雅关闭线程池
     * @return org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
     * @author dongfang.ding
     * @date 2019/12/11 0011 17:59
     **/
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
            int corePoolSize, int maxPoolSize, boolean gracefulShutdown) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity, corePoolSize, maxPoolSize,
                new ThreadPoolExecutor.CallerRunsPolicy(), gracefulShutdown
        );
    }


    /**
     * 构建线程池参数
     *
     * @param prefix           线程池名称前缀
     * @param keepAliveSeconds 保持空闲时间
     * @param queueCapacity    队列大小
     * @param rejectedExecutionHandler 拒绝策略
     * @param gracefulShutdown 是否优雅关闭线程池
     * @return
     */
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
            RejectedExecutionHandler rejectedExecutionHandler, boolean gracefulShutdown) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity,
                Runtime.getRuntime().availableProcessors() + 1, Runtime.getRuntime().availableProcessors() * 2,
                rejectedExecutionHandler, gracefulShutdown
        );
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
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
            int corePoolSize, int maxPoolSize, RejectedExecutionHandler rejectedExecutionHandler, boolean gracefulShutdown) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix(prefix);
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
        if (gracefulShutdown) {
            THREAD_POOL_TASK_EXECUTOR.add(threadPoolTaskExecutor);
        }
        return threadPoolTaskExecutor;
    }



    /**
     * 构建定时任务线程池
     *
     * @param prefix
     * @param keepAliveSeconds
     * @return
     */
    public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int keepAliveSeconds) {
        return buildScheduledExecutorService(prefix, Runtime.getRuntime().availableProcessors() + 1,
                Runtime.getRuntime().availableProcessors() * 2, keepAliveSeconds, true
        );
    }

    /**
     * 构建定时任务线程池
     *
     * @param prefix
     * @param keepAliveSeconds
     * @return
     */
    public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int keepAliveSeconds, boolean gracefulShutdown) {
        return buildScheduledExecutorService(prefix, Runtime.getRuntime().availableProcessors() + 1,
                Runtime.getRuntime().availableProcessors() * 2, keepAliveSeconds, gracefulShutdown
        );
    }

    /**
     * 构建定时任务线程池
     *
     * @param prefix
     * @param corePoolSize
     * @param maxPoolSize
     * @return
     */
    public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int corePoolSize,
            int maxPoolSize, int keepAliveSeconds, boolean gracefulShutdown) {
        ThreadFactory namedThreadFactory = new CustomizableThreadFactory(prefix);
        ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize,
                namedThreadFactory
        );
        scheduledExecutorService.setMaximumPoolSize(maxPoolSize);
        scheduledExecutorService.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
        if (gracefulShutdown) {
            POOLS.add(scheduledExecutorService);
        }
        return scheduledExecutorService;
    }
}
