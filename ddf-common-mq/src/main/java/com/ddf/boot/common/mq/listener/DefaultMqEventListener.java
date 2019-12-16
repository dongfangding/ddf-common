package com.ddf.boot.common.mq.listener;

import com.ddf.boot.common.mq.config.MqMessageProperties;
import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 对消息事件的默认实现，落库（不百分百保证）提供状态跟踪$
 * <p>
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
 * @date 2019/12/16 0016 17:55
 */
@Component
public class DefaultMqEventListener implements MqEventListener {

    @Autowired
    private static MqMessageProperties messageProperties;


    /**
     * 接收消息时保存到队列中，使用线程持续消费队列来进行消息落库
     */
    public static final BlockingQueue<ListenerQueueEntity> MESSAGE_QUEUE = new ArrayBlockingQueue<>(messageProperties.getMessageQueueSize());


    /**
     * 消息发送成功的回调事件
     *
     * @param channel
     * @param message
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void sendSuccess(@NotNull Channel channel, @NotNull Message message, MqMessageWrapper<T> messageWrapper) {
        MESSAGE_QUEUE.add(ListenerQueueEntity.build(channel, message, messageWrapper, ListenerQueueEntity.MqEvent.SEND_SUCCESS));
    }

    /**
     * 消息发送失败时的回调事件
     *
     * @param channel
     * @param message
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void sendFailure(@NotNull Channel channel, @NotNull Message message, MqMessageWrapper<T> messageWrapper) {
        MESSAGE_QUEUE.add(ListenerQueueEntity.build(channel, message, messageWrapper, ListenerQueueEntity.MqEvent.SEND_FAILURE));
    }

    /**
     * 消息消费成功时的回调事件
     *
     * @param channel
     * @param message
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void consumerSuccess(@NotNull Channel channel, @NotNull Message message, MqMessageWrapper<T> messageWrapper) {
        MESSAGE_QUEUE.add(ListenerQueueEntity.build(channel, message, messageWrapper, ListenerQueueEntity.MqEvent.CONSUMER_SUCCESS));
    }

    /**
     * 消息消息失败时的回调事件
     *
     * @param channel
     * @param message
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void consumerFailure(@NotNull Channel channel, @NotNull Message message, MqMessageWrapper<T> messageWrapper) {
        MESSAGE_QUEUE.add(ListenerQueueEntity.build(channel, message, messageWrapper, ListenerQueueEntity.MqEvent.CONSUMER_FAILURE));
    }
}
