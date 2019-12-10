package com.ddf.boot.common.mq.helper;

import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import com.ddf.boot.common.mq.util.MqMessageUtil;
import com.ddf.boot.common.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * rabbit发送工具类$
 *
 * @author dongfang.ding
 * @date 2019/12/10 0010 20:23
 */
@Slf4j
public class RabbitTemplateHelper {

    private RabbitTemplateHelper() {}

    private static RabbitTemplate rabbitTemplate = SpringContextHolder.getBean(RabbitTemplate.class);

    /**
     * 简化发送操作和统一封装消息体
     * @param queueDefinition
     * @param body
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
     * @param queueDefinition
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/10 0010 22:48
     **/
    public static <T> void send(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) {
        rabbitTemplate.convertAndSend(queueDefinition.getExchangeName(), queueDefinition.getRouteKey(), messageWrapper);
    }
    
    /**
     * 发送封装好的消息体，但不是必须要发送成功的，即发送失败不会抛出异常！
     * @param queueDefinition
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/10 0010 22:50
     **/
    public static <T> void sendNotNecessary(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) {
        try {
            rabbitTemplate.convertAndSend(queueDefinition.getExchangeName(), queueDefinition.getRouteKey(), messageWrapper);
        } catch (Exception exception) {
            log.warn("sendNotNecessary发送消息失败！{}", messageWrapper, exception);
        }
    }
    
    /**
     * 由于MQ自实现的重投会放到队列头部，如果数据有问题，会循环消费影响后面的数据；因此不使用；
     *
     * 简单实现重投机制，手动消费如果失败，则直接拒绝消息，消息从队头丢弃后，重新将当前消息发送一次；变相实现重投；
     *
     * 注意，这只是一种简单的补偿机制，并不作为可靠性的存在；而且调用的时候一定要在拒绝之后再调用该方法；
     * 如果丢弃之后重新发送失败就失败了，千万不能再丢弃之前发送，否则发送成功，旧的数据丢弃失败，那么数据就会重复；
     *
     * @param queueDefinition
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/10 0010 23:06
     **/
    public static <T> void requeue(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) {
        if (queueDefinition == null || messageWrapper == null) return;
        messageWrapper.setRetryTimes(messageWrapper.getRetryTimes() + 1);
        sendNotNecessary(queueDefinition, messageWrapper);
    }
}
