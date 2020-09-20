package com.ddf.boot.common.mq.Initialize;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.SpringContextHolder;
import com.ddf.boot.common.core.util.StringUtil;
import com.ddf.boot.common.mq.config.MqMessageProperties;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import com.ddf.boot.common.mq.entity.LogMqListener;
import com.ddf.boot.common.mq.listener.DefaultMqEventListener;
import com.ddf.boot.common.mq.listener.ListenerQueueEntity;
import com.ddf.boot.common.mq.listener.MqEventListener;
import com.ddf.boot.common.mq.persistence.LogMqPersistenceProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 根据预定义的队列/交换器/路由键信息声明队列
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
 * @date 2019/7/31 17:14
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AmqpDeclareBean implements InitializingBean {

    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    @Qualifier("defaultMqEventListener")
    private MqEventListener defaultMqEventListener;
    @Autowired
    private MqMessageProperties mqMessageProperties;

    /**
     * Bean初始化后的调用
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        buildQueueDefinition();
        initListenerQueue();
    }

    /**
     * 根据预定义的队列配置自动创建队列和交换器以及绑定他们的关系
     *
     * @author dongfang.ding
     * @date 2019/7/31 19:20
     */
    private void buildQueueDefinition() {
        log.debug("start buildQueueDefinition>>>>>>>>>>>>>");
        QueueBuilder.QueueDefinition[] values = QueueBuilder.QueueDefinition.values();
        if (values.length > 0) {
            for (QueueBuilder.QueueDefinition value : values) {
                Queue queue = new Queue(value.getQueueName(), true, false, false, value.getQueueArguments());
                Exchange exchange;
                if (QueueBuilder.ExchangeType.DIRECT.equals(value.getExchangeType())) {
                    exchange = new DirectExchange(value.getExchangeName(), true, false, value.getExchangeArguments());
                } else if (QueueBuilder.ExchangeType.FANOUT.equals(value.getExchangeType())) {
                    exchange = new FanoutExchange(value.getExchangeName(), true, false, value.getExchangeArguments());
                } else if (QueueBuilder.ExchangeType.TOPIC.equals(value.getExchangeType())) {
                    exchange = new TopicExchange(value.getExchangeName(), true, false, value.getExchangeArguments());
                } else {
                    throw new RuntimeException("暂不支持的交换器类型!");
                }
                // TODO remove it 为了方便调试参数构建队列的参数
                amqpAdmin.deleteQueue(value.getQueueName());
                amqpAdmin.declareQueue(queue);
                amqpAdmin.declareExchange(exchange);
                if (value.getBindingArguments() != null && !value.getBindingArguments().isEmpty()) {
                    amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(value.getRouteKey()).and(value.getBindingArguments()));
                } else {
                    amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(value.getRouteKey()).noargs());
                }
            }
        }
    }


    /**
     * 监听mq事件产生的数据队列，进行持久化！
     *
     * @return void
     * @author dongfang.ding
     * @date 2019/12/20 0020 15:43
     **/
    private void initListenerQueue() {
        if (defaultMqEventListener != null && defaultMqEventListener instanceof DefaultMqEventListener) {
            Executors.newSingleThreadExecutor(ThreadFactoryBuilder.create().setDaemon(true).setNamePrefix("consumer-listener-queue").build()).execute(() -> {
                while (true) {
                    try {
                        ListenerQueueEntity<?> poll = DefaultMqEventListener.MESSAGE_QUEUE.poll();
                        if (poll == null) {
                            try {
                                // 做一个延迟,其实更好的方式是使用等待唤醒，目前既要支持读和写，而又不互斥，还没想到好的方案
                                Thread.sleep(200);
                            } catch (Exception ignored) {}
                        } else {
                            LogMqListener logMqListener = new LogMqListener();
                            if (poll.getMessageWrapper() == null) {
                                log.error("数据缺失！！{}", poll);
                                continue;
                            }
                            logMqListener.setMessageId(poll.getMessageWrapper().getMessageId());
                            logMqListener.setCreator(poll.getMessageWrapper().getCreator());
                            logMqListener.setRequeueTimes(poll.getMessageWrapper().getRequeueTimes());
                            logMqListener.setMessageJson(JsonUtil.asString(poll.getMessageWrapper()));
                            logMqListener.setEvent(poll.getMqEvent().name());
                            logMqListener.setEventTimestamp(poll.getTimestamp());
                            if (ListenerQueueEntity.MqEvent.SEND_SUCCESS.equals(poll.getMqEvent()) ||
                                    ListenerQueueEntity.MqEvent.SEND_FAILURE.equals(poll.getMqEvent())) {
                                logMqListener.setSendTimestamp(poll.getTimestamp());
                            } else {
                                logMqListener.setConsumerTimestamp(poll.getTimestamp());
                            }

                            if (poll.getQueueDefinition() != null) {
                                logMqListener.setExchangeName(poll.getQueueDefinition().getExchangeName());
                                logMqListener.setExchangeType(poll.getQueueDefinition().getExchangeType().name());
                                logMqListener.setRouteKey(poll.getQueueDefinition().getRouteKey());
                                logMqListener.setTargetQueue(poll.getQueueDefinition().getQueueName());
                            }
                            if (poll.getRabbitListener() != null) {
                                logMqListener.setActualQueue(Arrays.toString(poll.getRabbitListener().queues()));
                                logMqListener.setContainerFactory(poll.getRabbitListener().containerFactory());
                            }
                            logMqListener.setCurrentThreadName(Thread.currentThread().getName());
                            logMqListener.setErrorMessage(poll.getThrowable() == null ? "" : poll.getThrowable().getMessage());
                            logMqListener.setErrorStack(poll.getThrowable() == null ? "" : StringUtil.exceptionToString(poll.getThrowable()));

                            Map<String, LogMqPersistenceProcessor> processorMap = SpringContextHolder.getBeansOfType(LogMqPersistenceProcessor.class);
                            if (CollUtil.isNotEmpty(processorMap) && processorMap.containsKey(mqMessageProperties.getLogMqPersistenceProcessorBeanName())) {
                                processorMap.get(mqMessageProperties.getLogMqPersistenceProcessorBeanName()).persistence(poll, logMqListener);
                            } else {
                                log.warn("没有配置mq监听消费持久化方案");
                            }
                        }
                    } catch (Exception e) {
                        log.error("持久化mq队列失败!", e);
                    }
                }
            });
        }
    }
}
