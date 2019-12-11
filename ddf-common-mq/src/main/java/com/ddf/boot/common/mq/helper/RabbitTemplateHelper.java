package com.ddf.boot.common.mq.helper;

import com.ddf.boot.common.mq.config.MqMessageProperties;
import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import com.ddf.boot.common.mq.util.MqMessageUtil;
import com.ddf.boot.common.util.SpringContextHolder;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

/**
 * rabbitMq发送工具类
 *
 * @author dongfang.ding
 * @date 2019/12/10 0010 20:23
 */
@Slf4j
public class RabbitTemplateHelper {

    private RabbitTemplateHelper() {
    }

    private static RabbitTemplate rabbitTemplate = SpringContextHolder.getBean(RabbitTemplate.class);

    private static MqMessageProperties mqMessageProperties = SpringContextHolder.getBean(MqMessageProperties.class);

    /**
     * 简化发送操作和统一封装消息体
     *
     * @param queueDefinition 要发送的队列定义
     * @param body            要发送的数据源对象，之际发送会使用{@link MqMessageWrapper} 包装
     * @return void
     * @author dongfang.ding
     * @date 2019/12/10 0010 20:28
     **/
    public static <T> void wrapperAndSend(QueueBuilder.QueueDefinition queueDefinition, T body) {
        MqMessageWrapper<T> wrapper = MqMessageUtil.wrapper(body);
        rabbitTemplate.convertAndSend(queueDefinition.getExchangeName(), queueDefinition.getRouteKey(), wrapper);
    }

    /**
     * 发送已经拼接好的统一消息体
     *
     * @param queueDefinition 要发送的队列定义
     * @param messageWrapper  要发送的已经被包装的数据
     * @return void
     * @author dongfang.ding
     * @date 2019/12/10 0010 22:48
     **/
    public static <T> void send(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) {
        rabbitTemplate.convertAndSend(queueDefinition.getExchangeName(), queueDefinition.getRouteKey(), messageWrapper);
    }

    /**
     * 发送封装好的消息体，但不是必须要发送成功的，即发送失败不会抛出异常！
     *
     * @param queueDefinition 需要发送到哪个队列
     * @param messageWrapper  要发送的数据
     * @return boolean  是否发送成功
     * @author dongfang.ding
     * @date 2019/12/10 0010 22:50
     **/
    public static <T> boolean sendNotNecessary(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) {
        try {
            rabbitTemplate.convertAndSend(queueDefinition.getExchangeName(), queueDefinition.getRouteKey(), messageWrapper);
            return true;
        } catch (Exception exception) {
            log.warn("sendNotNecessary发送消息失败！{}", messageWrapper, exception);
        }
        return false;
    }

    /**
     * 由于MQ自实现的重投会放到队列头部，如果数据有问题，会循环消费影响后面的数据；因此不使用；
     * <p>
     * 简单变相实现重投机制，手动消费如果失败，则直接拒绝消息，消息从队头丢弃后，重新将当前消息发送一次到队尾,有一个计数器，如果达到最大次数，不会再重投；
     * <p>
     * 注意，这只是一种简单的补偿机制，并不作为可靠性的存在；而且调用的时候一定要在拒绝之后再调用该方法；
     * 如果丢弃之后重新发送失败就失败了，千万不能再丢弃之前发送，否则发送成功，旧的数据丢弃失败，那么数据就会重复；
     *
     * @param queueDefinition 需要发送到哪个队列
     * @param messageWrapper  要重新投递的消息
     * @param consumer        如果发送失败之后，需要对原数据做什么额外的处理
     * @return void
     * @author dongfang.ding
     * @date 2019/12/10 0010 23:06
     **/
    public static <T> void requeue(@NotNull QueueBuilder.QueueDefinition queueDefinition
            , @NotNull MqMessageWrapper<T> messageWrapper, Consumer<MqMessageWrapper> consumer) {
        if (queueDefinition == null || messageWrapper == null || messageWrapper.getRequeueTimes() >= mqMessageProperties.getMaxRequeueTimes())
            return;
        messageWrapper.setRequeueTimes(messageWrapper.getRequeueTimes() + 1);
        boolean isSuccess = sendNotNecessary(queueDefinition, messageWrapper);
        if (!isSuccess) {
            consumer.accept(messageWrapper);
        }
    }

    /**
     * 拒绝消息然后执行重投
     *
     * @param channel         通道
     * @param message         消息对象
     * @param queueDefinition 要重投的队列定义
     * @param messageWrapper  要重投的消息
     * @param consumer        重投失败后的处理策略
     * @return void
     * @author dongfang.ding
     * @date 2019/12/11 0011 10:19
     **/
    public static <T> void nackAndRequeueIfFailure(@NotNull Channel channel, @NotNull Message message
            , @NotNull QueueBuilder.QueueDefinition queueDefinition, @NotNull MqMessageWrapper<T> messageWrapper
            , Consumer<MqMessageWrapper> consumer) {
        boolean isNack = false;
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            isNack = true;
        } catch (Exception e) {
            log.error("消息拒绝异常！{}", MqMessageUtil.getBodyAsString(message.getBody()), e);
        }
        // 拒绝成功执行重投
        if (isNack) {
            requeue(queueDefinition, messageWrapper, consumer);
        }
    }

    /**
     * 拒绝消息然后执行重投
     *
     * @param channel         通道
     * @param message         消息对象
     * @param queueDefinition 要重投的队列定义
     * @param messageWrapper  要重投的消息
     * @return void
     * @author dongfang.ding
     * @date 2019/12/11 0011 10:19
     **/
    public static <T> void nackAndRequeueIfFailure(@NotNull Channel channel, @NotNull Message message
            , @NotNull QueueBuilder.QueueDefinition queueDefinition, @NotNull MqMessageWrapper<T> messageWrapper) {
        nackAndRequeueIfFailure(channel, message, queueDefinition, messageWrapper, (data) -> log.error("消息重投失败: {}", data));
    }
}
