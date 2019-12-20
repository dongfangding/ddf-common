package com.ddf.boot.common.mq.listener;

import com.ddf.boot.common.mq.config.MqMessageProperties;
import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private static MqMessageProperties messageProperties;
    @Autowired
    @Qualifier("defaultEventListenerPool")
    private ThreadPoolTaskExecutor defaultEventListenerPool;


    public static void setMessageProperties(@Autowired MqMessageProperties messageProperties) {
        DefaultMqEventListener.messageProperties = messageProperties;
    }

    /**
     * 接收消息时保存到队列中，使用线程持续消费队列来进行消息落库
     * fixme
     */
    public static final BlockingQueue<ListenerQueueEntity> MESSAGE_QUEUE = new ArrayBlockingQueue<>(1000);

    /**
     * MESSAGE_QUEUE的同步锁，同时也用来实现等待与唤醒，有数据唤醒，无数据睡眠
     */
    public static final Object MESSAGE_QUEUE_LOCK = new Object();
    public static final ReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * 消息发送成功的回调事件
     *
     * @param queueDefinition
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void sendSuccess(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) {
        defaultEventListenerPool.execute(() -> {
            synchronized (DefaultMqEventListener.class) {
                MESSAGE_QUEUE.add(ListenerQueueEntity.buildSend(messageWrapper, ListenerQueueEntity.MqEvent.SEND_SUCCESS,
                        queueDefinition, null));
            }
        });

    }

    /**
     * 消息发送失败时的回调事件
     *
     * @param queueDefinition
     * @param messageWrapper
     * @param throwable
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void sendFailure(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper
            , Throwable throwable) {
        defaultEventListenerPool.execute(() -> {
            synchronized (DefaultMqEventListener.class) {
                MESSAGE_QUEUE.add(ListenerQueueEntity.buildSend(messageWrapper, ListenerQueueEntity.MqEvent.SEND_FAILURE,
                        queueDefinition, throwable));
            }
        });

    }

    /**
     * 消息消费成功时的回调事件
     *
     * @param rabbitListener
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void consumerSuccess(RabbitListener rabbitListener, MqMessageWrapper<T> messageWrapper) {
        defaultEventListenerPool.execute(() -> {
            synchronized (DefaultMqEventListener.class) {
                MESSAGE_QUEUE.add(ListenerQueueEntity.buildConsumer(messageWrapper, ListenerQueueEntity.MqEvent.CONSUMER_SUCCESS,
                        rabbitListener, null));
            }
        });

    }

    /**
     * 消息消息失败时的回调事件
     *
     * @param rabbitListener
     * @param messageWrapper
     * @param throwable
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    @Override
    public <T> void consumerFailure(RabbitListener rabbitListener, MqMessageWrapper<T> messageWrapper
            , Throwable throwable) {
        defaultEventListenerPool.execute(() -> {
            synchronized (DefaultMqEventListener.class) {
                MESSAGE_QUEUE.add(ListenerQueueEntity.buildConsumer(messageWrapper, ListenerQueueEntity.MqEvent.CONSUMER_FAILURE,
                        rabbitListener, throwable));
            }
        });
    }
}
