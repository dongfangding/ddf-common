package com.ddf.boot.common.mq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
     * 设置RabbitTemplate属性
     *
     * FIXME
     * Description:
     *
     * The dependencies of some of the beans in the application context form a cycle:
     *
     * ┌─────┐
     * |  rabbitTemplate defined in class path resource [com/ddf/boot/common/mq/config/MqConfig.class]
     * └─────┘
     *
     * @param rabbitTemplate
     * @return org.springframework.amqp.rabbit.core.RabbitTemplate
     * @author dongfang.ding
     * @date 2019/12/10 0010 14:24
     **/
//    @Bean
//    public RabbitTemplate rabbitTemplate(RabbitTemplate rabbitTemplate, CustomizeMessagePostProcessor messagePostProcessor) {
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//        rabbitTemplate.addBeforePublishPostProcessors(messagePostProcessor);
//        return rabbitTemplate;
//    }

    /**
     * 设置rabbitmq的序列化机制为application/json
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

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
     * 开启手动ack之后，如果消费端不调用basicAck方法，则消息会一直处理unack状态，而如果处理失败之后调用basicNack或basicReject将requeue的值设置为
     * 	 true之后消息会被自动设置回队列，而且是队列头部，这样就会导致如果该条消息会一直报错，那么就会造成无限重投和失败，而如果设置为false，则该条消息会直接删除;
     * 	 而如果不调用的话，该消息的状态会为unack
     *
     * 	 解决方案之一：
     * 	 最好不要重新投递，消费成功的就直接ack，而如果消费失败的，那么就将消费失败的消息保存到本地数据库中或者什么的业务逻辑处理，然后再将消息删除
     *
     *   <pre class="code">
     *       try {
     * 			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
     * 			// 开启了手动确认之后，要自己编码确认消息已收到,如果有自己的业务逻辑，则处理完业务逻辑之后再手动确认
     * 			logger.info("receiveFromQueue队列消费到消息.....{}", msg);
     *       } catch (Exception e) {
     * 			logger.error("消息消费异常: {}", new String(message.getBody(), StandardCharsets.UTF_8), e);
     * 			// deliveryTag 可以认为是消息的唯一身份标识符，multiple如果为true，则将此消息和此消息之前的所有数据都执行当前当前方法，
     * 			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
     * 			throw new RuntimeException(e);
     *        }
     *   </pre>
     *
     *
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
        // 手动ack最好设置为1
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
        manualRabbitListenerContainerFactory.setConcurrentConsumers(1);
        manualRabbitListenerContainerFactory.setMaxConcurrentConsumers(3);
        manualRabbitListenerContainerFactory.setStartConsumerMinInterval(2000L);
        manualRabbitListenerContainerFactory.setStopConsumerMinInterval(5000L);
        manualRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.NONE);
        manualRabbitListenerContainerFactory.setPrefetchCount(1);
        return manualRabbitListenerContainerFactory;
    }



    private void setDefaultConcurrentConsumerProperties() {

    }

}
