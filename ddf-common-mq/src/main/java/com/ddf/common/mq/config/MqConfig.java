package com.ddf.common.mq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mq的配置类
 *
 * @author dongfang.ding
 * @date 2019/8/1 18:28
 */
@Configuration
public class MqConfig {

    /**
     * 配置消费端的策略，消息确认机制为AUTO，注意与{@link AcknowledgeMode#NONE}的区别
     *
     * @param rabbitConnectionFactory 该项目为基本配置包，因此IDE会提示没有这个类，必须将该项目依赖到有rabbitmq的连接信息项目中
     * @return
     * @see RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener#containerFactory
     */
    @Bean
    public RabbitListenerContainerFactory autoAckRabbitListenerContainerFactory(CachingConnectionFactory rabbitConnectionFactory) {
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        simpleRabbitListenerContainerFactory.setConnectionFactory(rabbitConnectionFactory);
        simpleRabbitListenerContainerFactory.setPrefetchCount(1);
        simpleRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return simpleRabbitListenerContainerFactory;
    }


    /**
     * 配置消费端的策略，消息确认机制为手动ack
     *
     * @param rabbitConnectionFactory 该项目为基本配置包，因此IDE会提示没有这个类，必须将该项目依赖到有rabbitmq的连接信息项目中
     * @return
     * @see RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener#containerFactory
     */
    @Bean
    public RabbitListenerContainerFactory manualAckRabbitListenerContainerFactory(CachingConnectionFactory rabbitConnectionFactory) {
        SimpleRabbitListenerContainerFactory manualRabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        manualRabbitListenerContainerFactory.setConnectionFactory(rabbitConnectionFactory);
        manualRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        manualRabbitListenerContainerFactory.setPrefetchCount(1);
        return manualRabbitListenerContainerFactory;
    }


    /**
     * 配置消费端的策略，消息确认机制为NONE，即autoAck=true，注意与{@link AcknowledgeMode#AUTO}的区别
     *
     * @param rabbitConnectionFactory 该项目为基本配置包，因此IDE会提示没有这个类，必须将该项目依赖到有rabbitmq的连接信息项目中
     * @return
     * @see RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener#containerFactory
     */
    @Bean
    public RabbitListenerContainerFactory noneAckRabbitListenerContainerFactory(CachingConnectionFactory rabbitConnectionFactory) {
        SimpleRabbitListenerContainerFactory manualRabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        manualRabbitListenerContainerFactory.setConnectionFactory(rabbitConnectionFactory);
        manualRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.NONE);
        manualRabbitListenerContainerFactory.setPrefetchCount(1);
        return manualRabbitListenerContainerFactory;
    }

}
