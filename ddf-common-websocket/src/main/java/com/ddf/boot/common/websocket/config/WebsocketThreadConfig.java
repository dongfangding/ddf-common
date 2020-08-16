package com.ddf.boot.common.websocket.config;

import com.ddf.boot.common.core.helper.ThreadBuilderHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 线程池配置
 *
 * @author dongfang.ding
 * @date 2019/12/11 0011 18:03
 */
@Configuration
public class WebsocketThreadConfig {


    /**
     * 批量指令对设备发送的逻辑组装线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor batchCmdExecutor() {
        return ThreadBuilderHelper.buildThreadExecutor("batch-cmd-pool-", 60, 2000);
    }


    /**
     * 处理接收到消息之后的任务线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor handlerMessagePool() {
        return ThreadBuilderHelper.buildThreadExecutor("handler-message-pool-", 60, 20000);
    }

    /**
     * 处理接收到匹配模板的单个任务线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor handlerMatchTemplateExecutor() {
        return ThreadBuilderHelper.buildThreadExecutor("handler-match-template-", 60, 20000);
    }

    /**
     * 处理与APP通讯的消息异步处理
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor handlerMessageBusiness() {
        return ThreadBuilderHelper.buildThreadExecutor("handler-message-business-", 60, 20000);
    }

    /**
     * 处理接收到之后消息记录日志记录的线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor channelTransferPool() {
        return ThreadBuilderHelper.buildThreadExecutor("channel-transfer-pool-", 60, 10000);
    }


    /**
     * 消息推送任务线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor wsSendMessagePool() {
        return ThreadBuilderHelper.buildThreadExecutor("ws-send-msg-pool-", 60, 2000);
    }

    /**
     * 设备指令监控持久化发送数据的线程池
     *
     * @return
     */
    @Bean("deviceCmdRunningStatePersistencePool")
    public ThreadPoolTaskExecutor deviceCmdRunningStatePersistencePool() {
        return ThreadBuilderHelper.buildThreadExecutor("running-state-persistence-pool-", 60, 2000);
    }
}
