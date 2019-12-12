package com.ddf.boot.common.mq.helper;

import com.ddf.boot.common.mq.config.MqMessageProperties;
import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import com.ddf.boot.common.mq.exception.MqSendException;
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
    public static <T> void wrapperAndSend(QueueBuilder.QueueDefinition queueDefinition, T body) throws MqSendException {
        try {
            MqMessageWrapper<T> wrapper = MqMessageUtil.wrapper(body);
            rabbitTemplate.convertAndSend(queueDefinition.getExchangeName(), queueDefinition.getRouteKey(), wrapper);
        } catch (Exception e) {
            log.error("消息发送异常！ {}", body, e);
            throw new MqSendException(e.getMessage());
        }
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
    public static <T> void send(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) throws MqSendException {
        try {
            rabbitTemplate.convertAndSend(queueDefinition.getExchangeName(), queueDefinition.getRouteKey(), messageWrapper);
        } catch (Exception e) {
            log.error("消息发送异常！ {}", messageWrapper, e);
            throw new MqSendException(e.getMessage());
        }
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
     *
     * 该方法坑非常多！！！！一定要注意看注释！！！！！！！！
     *
     * 由于MQ自实现的重投会放到队列头部，如果数据有问题，会循环消费影响后面的数据；因此不使用；
     * <p>
     * 简单变相实现重投机制，手动消费如果失败，则直接拒绝消息，消息从队头丢弃后，重新将当前消息发送一次到队尾,有一个计数器，如果达到最大次数，不会再重投；
     * <p>
     * 注意：！！！！！！！
     *     1. 如果当前队列是死信队列，那么每次消费失败，再重投都会导致产生一条新的数据被路由到消费当前死信队列的另外一个队列中；
     *          但由于消费失败转死信目前无法控制，所以不要在死信队列上调用该方法！！
     *          如果想要调用，重复的消息会保障消息id是一致的，如果消费端会根据这个做幂等性，也是可以的；要谨慎使用！
     *
     *     2. 如果配置了多消费者，在消息重投多次后，如果有两个消费者每个都拿取了其中的一条重试数据，那么一个消费者消费成功，一个消费失败，
     *          消息又会进行重投。同样会出现数据重复问题。。。。Orz......
     *
     *
     *     3. 这只是一种简单的补偿机制，并不作为可靠性的存在；而且调用的时候一定要在拒绝之后再调用该方法；
     *          如果丢弃之后重新发送失败就失败了，千万不能再丢弃之前发送，否则发送成功，旧的数据丢弃失败，那么数据就会重复；
     *          这一块在方法{@link RabbitTemplateHelper#nackAndRequeue(com.rabbitmq.client.Channel,
     *          org.springframework.amqp.core.Message, com.ddf.boot.common.mq.definition.QueueBuilder.QueueDefinition,
     *          com.ddf.boot.common.mq.definition.MqMessageWrapper, java.util.function.Consumer)}封装的时候已经避免
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
        log.info("重投消息: {}", messageWrapper);
        messageWrapper.setRequeueTimes(messageWrapper.getRequeueTimes() + 1);
        boolean isSuccess = sendNotNecessary(queueDefinition, messageWrapper);
        if (!isSuccess) {
            log.error("消息重投失败！{}", messageWrapper);
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
    public static <T> void nackAndRequeue(@NotNull Channel channel, @NotNull Message message
            , @NotNull QueueBuilder.QueueDefinition queueDefinition, @NotNull MqMessageWrapper<T> messageWrapper
            , Consumer<MqMessageWrapper> consumer) {
        boolean isNack = false;
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            isNack = true;
        } catch (Exception e) {
            // 如果消息拒绝异常会发生什么？经测试，拒绝时如果出现异常（IDEA断点，然后停掉mq服务再执行拒绝操作），消息此时会变为unack状态，待服务
            // 回复后，消息状态会重回ready状态
            log.error("消息拒绝异常，无法重投！！{}", MqMessageUtil.getBodyAsString(message.getBody()), e);
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
    public static <T> void nackAndRequeue(@NotNull Channel channel, @NotNull Message message
            , @NotNull QueueBuilder.QueueDefinition queueDefinition, @NotNull MqMessageWrapper<T> messageWrapper) {
        nackAndRequeue(channel, message, queueDefinition, messageWrapper, (data) -> log.error("消息重投失败: {}", data));
    }
}
