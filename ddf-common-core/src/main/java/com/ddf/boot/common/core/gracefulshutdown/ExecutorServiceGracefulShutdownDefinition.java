package com.ddf.boot.common.core.gracefulshutdown;

import com.ddf.boot.common.core.helper.ThreadBuilderHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CollectionUtils;

/**
 * <p>参考{@link ExecutorConfigurationSupport#shutdown()} 实现的对线程池优雅关闭的注册类</p >
 * <p>Spring已经实现了线程池的优雅关闭逻辑， 需要满足几个前提和注意事项</p >
 *      <li>1. 使用Spring封装的线程池类， 如{@link ThreadPoolTaskExecutor}, 线程池必须交由Spring容器管理， 优雅关闭的方法是在父类中{@link ExecutorConfigurationSupport#shutdown()}定义的</li>
 *      <li>2. 必须调用线程池父类的{@link ExecutorConfigurationSupport#setWaitForTasksToCompleteOnShutdown(boolean)}
 *             和{@link ExecutorConfigurationSupport#setAwaitTerminationSeconds(int)}方法来满足优雅关闭的判断前提</li>
 *      <li>3. 注意容器销毁顺序， 如果线程池中使用需要使用数据源，则必须保证数据源在线程池后面被销毁，{@link Order}似乎不是你用来控制容器初始化顺序的，想来应该也和容器依赖有关，根本无法保证有序</li>
 *      <li>4. 具体Spring的关闭钩子方法逻辑在{@link org.springframework.context.support.AbstractApplicationContext#close()},
 *       在关闭单例池时执行到{@link DefaultSingletonBeanRegistry#destroySingletons()}时有个属性{@link DefaultSingletonBeanRegistry#disposableBeans},
 *       销毁的时候是按照这个顺序来定义的，而且这个属性本身有序，这个属性里存的bean都是实现了Spring生命周期相关方法的bean,
 *       具体逻辑见{@link AbstractBeanFactory#registerDisposableBeanIfNecessary(java.lang.String, java.lang.Object, org.springframework.beans.factory.support.RootBeanDefinition)}</li>
 *  <p></p>
 *
 *  <p>
 *      如果不使用上述方式， 则本类提供另外一种逻辑， 即依赖于{@link org.springframework.context.support.AbstractApplicationContext#close()}
 *  方法中定义的逻辑，在Spring容器关闭的前面会先发布一个容器关闭事件， 则可以监听容器关闭事件，在容器关闭事件中将Spring实现的线程池
 *  关闭的方法抄过来， 这样可以用来处理一些未交由Spring容器管理的线程池，而且该类保证线程池的关闭逻辑一定是早于所有Spring容器的，因为不存在
 *  上述方案需要定义线程池优先级的问题
 *  </p>
 *
 *  <p></p>
 * <p><b>NOTE:</b></p>
 * <p>1. 如果是web项目， 停机无法解决流量继续转发进来的问题， 如nginx, 需要配合运维手段将发布机器从nginx负载中下线</p>
 * <p>2. 如果是Dubbo项目，需要先执行Dubbo的优雅停机，确保先将提供者从注册中心移除，不再有新的消费者请求进来</p>
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/15 13:02
 */
@Slf4j
public class ExecutorServiceGracefulShutdownDefinition implements ApplicationListener<ContextClosedEvent> {

    private static final List<ExecutorService> POOLS = Collections.synchronizedList(new ArrayList<>(12));

    private final long awaitTermination;

    private final TimeUnit timeUnit;

    public ExecutorServiceGracefulShutdownDefinition(long awaitTermination, TimeUnit timeUnit) {
        this.awaitTermination = awaitTermination;
        this.timeUnit = timeUnit;
    }

    /**
     * 注册要关闭的线程池， 如果一些线程池未交由线程池管理，则可以调用这个方法
     *
     * @param executor
     */
    public static void registryExecutor(ThreadPoolExecutor executor) {
        POOLS.add(executor);
    }

    /**
     * 注册要关闭的线程池
     * 注意如果调用这个方法的话，而线程池又是由Spring管理的，则必须等待这个bean初始化完成后才可以调用
     * 因为依赖的{@link ThreadPoolTaskExecutor#getThreadPoolExecutor()}必须要在bean的父类方法中定义的
     * 初始化{@link ExecutorConfigurationSupport#afterPropertiesSet()}方法中才会赋值
     *
     * @param threadPoolTaskExecutor
     */
    public static void registryExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        POOLS.add(threadPoolTaskExecutor.getThreadPoolExecutor());
    }

    /**
     * 注册要关闭的线程池
     * 注意如果调用这个方法的话，而线程池又是由Spring管理的，则必须等待这个bean初始化完成后才可以调用
     * 因为依赖的{@link ThreadPoolTaskExecutor#getThreadPoolExecutor()}必须要在bean的父类方法中定义的
     * 初始化{@link ExecutorConfigurationSupport#afterPropertiesSet()}方法中才会赋值
     *
     * 重写了{@link ThreadPoolTaskScheduler#initializeExecutor(java.util.concurrent.ThreadFactory, java.util.concurrent.RejectedExecutionHandler)}
     * 来对父类的{@link ExecutorConfigurationSupport#executor}赋值
     *
     * @param threadPoolTaskExecutor
     */
    public static void registryExecutor(ThreadPoolTaskScheduler threadPoolTaskExecutor) {
        POOLS.add(threadPoolTaskExecutor.getScheduledThreadPoolExecutor());
    }

    /**
     * 参考{@link ExecutorConfigurationSupport#shutdown()}
     * 问题
     * 1. 感觉他么的跟在这里睡眠60秒没啥区别，当然如果线程池里没有任务或者剩余任务的时间小于60s，还是有点用处的，
     * 至少这个时间针对现在这样的逻辑是最大时间，而如果sleep则是固定时间，但至少也能解决问题啊。
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // 将通过帮助类创建的线程池现在统一添加进来也进行
        POOLS.addAll(ThreadBuilderHelper.getPools());
        log.info("容器关闭前处理线程池优雅关闭开始, 当前要处理的线程池数量为: {} >>>>>>>>>>>>>>>>", POOLS.size());
        if (CollectionUtils.isEmpty(POOLS)) {
            return;
        }
        for (ExecutorService pool : POOLS) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(awaitTermination, timeUnit)) {
                    if (log.isWarnEnabled()) {
                        log.warn("Timed out while waiting for executor [{}] to terminate", pool);
                    }
                }
            }
            catch (InterruptedException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Timed out while waiting for executor [{}] to terminate", pool);
                }
                Thread.currentThread().interrupt();
            }
        }
    }
}
