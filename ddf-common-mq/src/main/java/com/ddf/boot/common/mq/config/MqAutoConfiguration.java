package com.ddf.boot.common.mq.config;

import com.ddf.boot.common.core.helper.ThreadBuilderHelper;
import com.ddf.boot.common.mq.definition.BindingConst;
import com.ddf.boot.common.mq.helper.RabbitTemplateHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * mq的配置类
 * <p>
 * https://www.docs4dev.com/docs/zh/spring-amqp/2.1.2.RELEASE/reference/_reference.html
 * <p>
 * TODO
 * 1. 持久化和发送是否同步提供参数，对于想要保证数据库和实际发送保持一致的使用者来说，可以使用同步模式，先持久化后发送
 * 2. 存储消息的队列分开，每个事件使用单独的队列，否则持久化的速度有点感人
 * <p>
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
 * @date 2019/8/1 18:28
 */
@Configuration
@MapperScan(basePackages = {"com.ddf.boot.common.mq.mapper"})
@ComponentScan(basePackages = "com.ddf.boot.common.mq")
public class MqAutoConfiguration {


    /**
     * 默认出现mq事件的线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor defaultEventListenerPool() {
        return ThreadBuilderHelper.buildThreadExecutor("default-event-listener-pool-", 60, 1000);
    }


    /**
     * 设置RabbitTemplate属性
     *
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
     *
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置消费端的策略，消息确认机制为AUTO，注意与{@link AcknowledgeMode#NONE}的区别
     * 该模式的消息确认会根据容器是否抛出异常来决定是调用ack还是nack， 如果消费正常，则消息被删除；
     * 如果消费过程中出现异常，那么消息状态会转为Unacked， 然后如果有消费这，消息会再次变为Ready等待消费；重复这个过程；
     * 类似与手动模式开启requeue
     *
     * @param rabbitConnectionFactory 该项目为基本配置包，因此IDE会提示没有这个类，必须将该项目依赖到有rabbitmq的连接信息项目中
     * @return
     * @author dongfang.ding
     * @date 2019/12/12 0012 15:40
     * @see RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener#containerFactory
     */
    @Bean(BindingConst.ACK_MODE_SINGLE_AUTO_ACK)
    public RabbitListenerContainerFactory singleAutoAck(CachingConnectionFactory rabbitConnectionFactory) {
        SimpleRabbitListenerContainerFactory rabbitListener = buildRabbitListener(rabbitConnectionFactory,
                AcknowledgeMode.AUTO, 250, 1, 1, 0L, 0L
        );
        // 要小于或等于prefetchCount,一个事务中要处理的消息数,这个值暂未理解影响程度
//        rabbitListener.setTxSize(100);
        return rabbitListener;
    }

    /**
     * 配置并发消费端的策略，消息确认机制为auto
     *
     * @param rabbitConnectionFactory
     * @return org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory
     * @author dongfang.ding
     * @date 2019/12/12 0012 15:40
     **/
    @Bean(BindingConst.ACK_MODE_CONCURRENT_AUTO_ACK)
    public RabbitListenerContainerFactory concurrentAutoAck(CachingConnectionFactory rabbitConnectionFactory) {
        return buildRabbitListener(rabbitConnectionFactory, AcknowledgeMode.AUTO, 250, 2,
                Runtime.getRuntime().availableProcessors(), 10000L, 60000L
        );
    }


    /**
     * 配置消费端的策略，消息确认机制为手动ack
     * <p>
     * 默认最小和最大消费者数量都为1
     * <p>
     * 开启手动ack之后，如果消费端不调用basicAck方法，则消息会一直处理Unacked状态，而如果处理失败之后调用basicNack或basicReject将requeue的值设置为
     * true之后消息会被自动设置回队列，而且是队列头部，如果数据本身有问题，这样就会导致该条消息会一直报错，那么就会造成无限重投和失败，而如果设置为false，则该条消息会直接删除;
     * 而如果不调用ack或nack的话，该消息的状态会为unack
     * <p>
     * 解决方案之一：
     * 最好不要重新投递，消费成功的就直接ack，而如果消费失败的，那么就将消费失败的消息保存到本地数据库中或者死信队列，然后再做处理最后将消息删除
     * <p>
     * 本模块包提供一个消费失败后的简单重投实现，可以参考{@link RabbitTemplateHelper#nackAndRequeue(com.rabbitmq.client.Channel, org.springframework.amqp.core.Message, com.ddf.boot.common.mq.definition.QueueBuilder.QueueDefinition, com.ddf.boot.common.mq.definition.MqMessageWrapper, java.util.function.Consumer)}
     *
     * <pre class="code">
     *       try {
     * 			logger.info("receiveFromQueue队列消费到消息.....{}", msg);
     * 		    // 开启了手动确认之后，要自己编码确认消息已收到,如果有自己的业务逻辑，则处理完业务逻辑之后再手动确认
     * 		    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
     *       } catch (Exception e) {
     * 			logger.error("消息消费异常: {}", new String(message.getBody(), StandardCharsets.UTF_8), e);
     * 			// deliveryTag 可以认为是消息的唯一身份标识符，multiple如果为true，则将此消息和此消息之前的所有数据都执行当前当前方法，
     * 			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
     * 		    // catch是为了做异常处理，但还是要将异常抛出去，让容器感知到消费失败
     * 		    throw new RuntimeException(e.getMessage());
     *        }
     *  </pre>
     *
     * @param rabbitConnectionFactory 该项目为基本配置包，因此IDE会提示没有这个类，必须将该项目依赖到有rabbitmq的连接信息项目中
     * @return
     * @author dongfang.ding
     * @date 2019/12/12 0012 15:40
     * @see RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener#containerFactory
     */
    @Bean(BindingConst.ACK_MODE_SINGLE_MANUAL_ACK)
    public RabbitListenerContainerFactory singleManualAck(CachingConnectionFactory rabbitConnectionFactory) {
        return buildRabbitListener(rabbitConnectionFactory, AcknowledgeMode.MANUAL, 1, 1, 1, 0L, 0L);
    }


    /**
     * 配置并发消费端的策略，消息确认机制为手动ack
     *
     * @param rabbitConnectionFactory
     * @return org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory
     * @author dongfang.ding
     * @date 2019/12/12 0012 15:40
     **/
    @Bean(BindingConst.ACK_MODE_CONCURRENT_MANUAL_ACK)
    public RabbitListenerContainerFactory concurrentManualAck(CachingConnectionFactory rabbitConnectionFactory) {
        return buildRabbitListener(rabbitConnectionFactory, AcknowledgeMode.MANUAL, 1, 2,
                Runtime.getRuntime().availableProcessors(), 10000L, 60000L
        );
    }


    /**
     * 配置消费端的策略，消息确认机制为NONE，即autoAck=true，注意与{@link AcknowledgeMode#AUTO}的区别
     * <p>
     * 无论消费过程中成功还是失败，消息都直接丢弃！
     *
     * @param rabbitConnectionFactory 该项目为基本配置包，因此IDE会提示没有这个类，必须将该项目依赖到有rabbitmq的连接信息项目中
     * @return
     * @author dongfang.ding
     * @date 2019/12/12 0012 15:40
     * @see RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener#containerFactory
     */
    @Bean(BindingConst.ACK_MODE_NONE_ACK)
    public RabbitListenerContainerFactory noneAck(CachingConnectionFactory rabbitConnectionFactory) {
        return buildRabbitListener(rabbitConnectionFactory, AcknowledgeMode.NONE, 1, 1,
                Runtime.getRuntime().availableProcessors(), 10000L, 60000L
        );
    }

    /**
     * 并发AcknowledgeMode.NONE
     *
     * @param rabbitConnectionFactory
     * @return org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory
     * @author dongfang.ding
     * @date 2019/12/16 0016 16:10
     **/
    @Bean(BindingConst.ACK_MODE_CONCURRENT_NONE_ACK)
    public RabbitListenerContainerFactory concurrentNoneAck(CachingConnectionFactory rabbitConnectionFactory) {
        return buildRabbitListener(rabbitConnectionFactory, AcknowledgeMode.NONE, 1, 1,
                Runtime.getRuntime().availableProcessors(), 10000L, 60000L
        );
    }


    /**
     * 设置SimpleRabbitListenerContainerFactory属性
     *
     * @param rabbitConnectionFactory 连接工厂
     * @param acknowledgeMode         ack模式
     * @param prefetchCount           一个消费者请求预取数量
     * @param concurrency             最小并发消费者数量
     * @param maxConcurrency          最大并发消费者数量
     * @param minStartInterval        启动新消费者的最小时间
     * @param minStopInterval         停止多余最小消费者数量意外的消费者的空闲时间
     * @return org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
     * @author dongfang.ding
     * @date 2019/12/12 0012 10:49
     **/
    private SimpleRabbitListenerContainerFactory buildRabbitListener(CachingConnectionFactory rabbitConnectionFactory,
            AcknowledgeMode acknowledgeMode, Integer prefetchCount, Integer concurrency, Integer maxConcurrency,
            Long minStartInterval, Long minStopInterval) {
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory =
                new SimpleRabbitListenerContainerFactory();
        simpleRabbitListenerContainerFactory.setConnectionFactory(rabbitConnectionFactory);
        // ack模式
        simpleRabbitListenerContainerFactory.setAcknowledgeMode(acknowledgeMode);
        // 决定了在一个请求中可以发送给消费者多少条消息，通常可以设置很高以提高吞吐量；2.0版本好像默认250, ack模式none无效， 手动ack最好为1
        simpleRabbitListenerContainerFactory.setPrefetchCount(prefetchCount);
        // 最小并发消费者数量
        simpleRabbitListenerContainerFactory.setConcurrentConsumers(concurrency);
        // 最大并发消费者数量
        simpleRabbitListenerContainerFactory.setMaxConcurrentConsumers(maxConcurrency);
        if (!concurrency.equals(maxConcurrency)) {
            // 如果最大消费者大于最小消费者数量，且当前未达到最大消费者，那么最小启动一个新的消费者的时间
            simpleRabbitListenerContainerFactory.setStartConsumerMinInterval(minStartInterval);
            // 如果最大消费者数量超过最小，那么消费者空闲多久会被回收掉
            simpleRabbitListenerContainerFactory.setStopConsumerMinInterval(minStopInterval);
        }
        return simpleRabbitListenerContainerFactory;
    }

}
