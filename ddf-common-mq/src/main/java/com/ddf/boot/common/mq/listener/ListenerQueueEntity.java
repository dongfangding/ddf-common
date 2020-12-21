package com.ddf.boot.common.mq.listener;

import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * 默认监听实现的阻塞队列中要保存的对象$
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
 * @date 2019/12/16 0016 18:07
 */
@Data
@Accessors(chain = true)
public class ListenerQueueEntity<T> {
    /**
     * 消息包装体
     */
    private MqMessageWrapper<T> messageWrapper;
    /**
     * 事件类型
     */
    private MqEvent mqEvent;
    /**
     * 队列定义信息
     */
    private QueueBuilder.QueueDefinition queueDefinition;
    /**
     * 消费端的配置注解
     */
    private RabbitListener rabbitListener;
    /**
     * 如果消费失败时将异常类放入进来
     */
    private Throwable throwable;

    /**
     * 放入的时间戳，避免同一个消息不同的状态被发送到不同的机器中导致数据先后执行顺序不一致的问题
     */
    private Long timestamp;


    /**
     * 发送时的数据构建参数
     *
     * @param messageWrapper
     * @param mqEvent
     * @param queueDefinition
     * @param <T>
     * @return
     */
    public static <T> ListenerQueueEntity<T> buildSend(MqMessageWrapper<T> messageWrapper, MqEvent mqEvent,
            QueueBuilder.QueueDefinition queueDefinition, Throwable throwable) {
        ListenerQueueEntity<T> entity = new ListenerQueueEntity<>();
        return entity.setMessageWrapper(messageWrapper)
                .setMqEvent(mqEvent)
                .setTimestamp(System.currentTimeMillis())
                .setQueueDefinition(queueDefinition)
                .setThrowable(throwable);
    }


    /**
     * 消费时的构建参数
     *
     * @param messageWrapper
     * @param mqEvent
     * @param rabbitListener
     * @param <T>
     * @param throwable
     * @return
     */
    public static <T> ListenerQueueEntity<T> buildConsumer(MqMessageWrapper<T> messageWrapper, MqEvent mqEvent,
            RabbitListener rabbitListener, Throwable throwable) {
        ListenerQueueEntity<T> entity = new ListenerQueueEntity<>();
        return entity.setMessageWrapper(messageWrapper)
                .setMqEvent(mqEvent)
                .setRabbitListener(rabbitListener)
                .setTimestamp(System.currentTimeMillis())
                .setThrowable(throwable);
    }


    public enum MqEvent {
        SEND_SUCCESS, SEND_FAILURE, CONSUMER_SUCCESS, CONSUMER_FAILURE
    }
}
