package com.ddf.common.mq.listeners;

import com.ddf.common.mq.definition.QueueBuilder;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 容器启动时执行事件
 *
 * @author dongfang.ding
 * @date 2019/7/31 17:14
 */
@Component
public class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        buildQueueDefinition();
    }


    /**
     * 根据预定义的队列配置自动创建队列和交换器以及绑定他们的关系
     * 
     * @author dongfang.ding
     * @date 2019/7/31 19:20 
     */
    private void buildQueueDefinition() {
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
}
