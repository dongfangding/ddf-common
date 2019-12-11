package com.ddf.boot.common.helper;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 创建线程
 *
 * @author dongfang.ding
 * @date 2019/12/11 0011 17:52
 */
public class ThreadBuilderHelper {

    /**
     * 构建线程池参数
     *
     * @param prefix           线程池名称前缀
     * @param keepAliveSeconds 保持空闲时间
     * @param queueCapacity    队列大小
     * @return
     */
    public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity) {
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity, Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2 + 1, new ThreadPoolExecutor.AbortPolicy());
    }


    /**
     * 构建线程池参数
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
        return buildThreadExecutor(prefix, keepAliveSeconds, queueCapacity, corePoolSize, maxPoolSize, new ThreadPoolExecutor.AbortPolicy());
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
}
