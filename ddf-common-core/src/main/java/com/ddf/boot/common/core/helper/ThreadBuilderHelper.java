package com.ddf.boot.common.core.helper;

import com.ddf.boot.common.core.gracefulshutdown.ExecutorServiceGracefulShutdownDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 创建线程帮助类
 * <p>
 * <p>线程池按照业务时间划分</p>
 *
 * <p>可承受线程数 ≈ CPU 核数 * (1 + 平均 IO 等待时间 / 平均 CPU 执行时间)</p>
 * <p>例如，如果每个请求平均：</p>
 * <p>业务逻辑用 CPU 100ms</p>
 * <p>调数据库和接口平均耗时 400ms（IO 阻塞）</p>
 * <p>那么计算下来：</p>
 * <p>1 * (1 + 400 / 100) = 1 * (1 + 4) = 5</p>
 * <p>理想线程数是 5 个左右</p>
 *
 * <p>任务队列设置</p>
 * <p>请求速率（QPS） QPS 高 → 队列大一点</p>
 * <p>任务执行时间 耗时长 → 队列大一点</p>
 * <p>是否允许排队等待 允许排队 → 队列大一点；希望快速失败 → 队列小一点</p>
 * 经验公式：队列容量 ≈ QPS × 平均响应时间（秒）
 *
 * @author dongfang.ding
 * @since 2019/12/11 0011 17:52
 */
@Slf4j
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
	private static final List<ThreadPoolTaskExecutor> THREAD_POOL_TASK_EXECUTOR = Collections.synchronizedList(
			new ArrayList<>(20));

	/**
	 * 需要定期打印运行状况的县城伺候
	 */
	private static final List<ThreadPoolTaskExecutor> PRINT_RUNNING_STATE_EXECUTOR = Collections.synchronizedList(
			new ArrayList<>(20));

	/**
	 * 返回通过该帮助类添加的线程池
	 *
	 * @return
	 */
	public static List<ExecutorService> getPools() {
		POOLS.addAll(THREAD_POOL_TASK_EXECUTOR
				.stream()
				.map(ThreadPoolTaskExecutor::getThreadPoolExecutor)
				.toList());
		return POOLS;
	}

	static {
		Executors
				.newSingleThreadScheduledExecutor()
				.scheduleAtFixedRate(
						() -> {
							if (!PRINT_RUNNING_STATE_EXECUTOR.isEmpty()) {
								log.info("线程池-运行状态监控打印开始");
								PRINT_RUNNING_STATE_EXECUTOR.forEach(executor -> {
									final ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
									log.info(
											"线程池[{}]: currentPoolSize: {}, corePoolSize: {}, maxPoolSize: {}, queueCapacity:{}, queueSize: {}, completedTaskCount:{}",
											executor.getThreadNamePrefix(), threadPoolExecutor.getPoolSize(),
											threadPoolExecutor.getCorePoolSize(),
											threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor
													.getQueue()
													.size(), executor.getQueueCapacity(),
											threadPoolExecutor.getCompletedTaskCount()
									);
								});
								log.info("线程池-运行状态监控打印结束");
							}
						}, 5, 5, TimeUnit.MINUTES
				);
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
		return buildThreadExecutor(
				prefix, keepAliveSeconds, queueCapacity, getDefaultCorePoolSize(),
				getDefaultMaxPoolSize(), new ThreadPoolExecutor.CallerRunsPolicy(), false, true
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
	public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
			boolean gracefulShutdown) {
		return buildThreadExecutor(
				prefix, keepAliveSeconds, queueCapacity, getDefaultCorePoolSize(), getDefaultMaxPoolSize(),
				new ThreadPoolExecutor.CallerRunsPolicy(), gracefulShutdown, true
		);
	}

	/**
	 * 构建线程池参数
	 *
	 * @param prefix                   线程池名称前缀
	 * @param keepAliveSeconds         保持空闲时间
	 * @param queueCapacity            队列大小
	 * @param rejectedExecutionHandler 拒绝策略
	 * @return
	 */
	public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
			RejectedExecutionHandler rejectedExecutionHandler) {
		return buildThreadExecutor(
				prefix, keepAliveSeconds, queueCapacity, getDefaultCorePoolSize(),
				getDefaultCorePoolSize() * 2, rejectedExecutionHandler, false, true
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
	 * @return org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
	 * @since 2019/12/11 0011 17:59
	 **/
	public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
			int corePoolSize, int maxPoolSize) {
		return buildThreadExecutor(
				prefix, keepAliveSeconds, queueCapacity, corePoolSize, maxPoolSize,
				new ThreadPoolExecutor.CallerRunsPolicy(), false, true
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
	 * @param gracefulShutdown         参数
	 * @return org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
	 * @since 2019/12/11 0011 17:59
	 **/
	public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
			int corePoolSize, int maxPoolSize, RejectedExecutionHandler rejectedExecutionHandler,
			boolean gracefulShutdown) {
		return buildThreadExecutor(
				prefix, keepAliveSeconds, queueCapacity, corePoolSize, maxPoolSize,
				rejectedExecutionHandler, gracefulShutdown, true
		);
	}

	/**
	 * 构建线程池参数
	 *
	 * @param prefix                    线程池名称前缀
	 * @param corePoolSize              核心线程池大小
	 * @param maxPoolSize               最大线程池大小
	 * @param keepAliveSeconds          保持空闲时间
	 * @param queueCapacity             队列大小
	 * @param rejectedExecutionHandler  队列满之后的处理策略
	 * @param gracefulShutdown          是否优雅关闭
	 * @param schedulePrintRunningState 是否需要定时打印运行状况
	 * @return org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
	 * @since 2019/12/11 0011 17:59
	 **/
	public static ThreadPoolTaskExecutor buildThreadExecutor(String prefix, int keepAliveSeconds, int queueCapacity,
			int corePoolSize, int maxPoolSize, RejectedExecutionHandler rejectedExecutionHandler,
			boolean gracefulShutdown, boolean schedulePrintRunningState) {
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
		if (schedulePrintRunningState) {
			PRINT_RUNNING_STATE_EXECUTOR.add(threadPoolTaskExecutor);
		}
		return threadPoolTaskExecutor;
	}


	/**
	 * 构建定时任务线程池
	 *
	 * @param prefix           前缀参数
	 * @param keepAliveSeconds keepaliveseconds参数
	 * @return
	 */
	public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int keepAliveSeconds) {
		return buildScheduledExecutorService(
				prefix, keepAliveSeconds, true, Runtime
						.getRuntime()
						.availableProcessors() + 1, Runtime
						.getRuntime()
						.availableProcessors() * 2
		);
	}

	/**
	 * 构建定时任务线程池
	 *
	 * @param prefix           前缀参数
	 * @param keepAliveSeconds keepaliveseconds参数
	 * @param gracefulShutdown 参数
	 * @return
	 */
	public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int keepAliveSeconds,
			boolean gracefulShutdown) {
		return buildScheduledExecutorService(
				prefix, keepAliveSeconds, gracefulShutdown, Runtime
						.getRuntime()
						.availableProcessors() + 1, Runtime
						.getRuntime()
						.availableProcessors() * 2
		);
	}

	/**
	 * 构建定时任务线程池
	 *
	 * @param prefix           前缀参数
	 * @param corePoolSize     corepool大小参数
	 * @param maxPoolSize      最大pool大小参数
	 * @param keepAliveSeconds 参数
	 * @param gracefulShutdown 参数
	 * @return
	 */
	public static ScheduledThreadPoolExecutor buildScheduledExecutorService(String prefix, int keepAliveSeconds,
			boolean gracefulShutdown, int corePoolSize, int maxPoolSize) {
		ThreadFactory namedThreadFactory = new CustomizableThreadFactory(prefix);
		ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(
				corePoolSize,
				namedThreadFactory
		);
		scheduledExecutorService.setMaximumPoolSize(maxPoolSize);
		scheduledExecutorService.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
		if (gracefulShutdown) {
			POOLS.add(scheduledExecutorService);
		}
		return scheduledExecutorService;
	}

	/**
	 * 获取默认核心线程池大小，结合常用业务逻辑低CPU +接口调用或者数据库操作的IO任务
	 *
	 * @return
	 */
	public static int getDefaultCorePoolSize() {
		final Runtime runtime = Runtime.getRuntime();
		int cores = runtime.availableProcessors();
		// 默认策略：每核分配 2~4 个线程
		return Math.max(2, cores * 2);
	}

	/**
	 * 获取默认最大线程池大小，结合常用业务逻辑低CPU +接口调用或者数据库操作的IO任务
	 *
	 * @return
	 */
	public static int getDefaultMaxPoolSize() {
		return getDefaultCorePoolSize() * 2;
	}
}
