package com.ddf.boot.common.mq.listener;

import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.amqp.core.Message;

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
    private Channel channel;
    private Message message;
    private MqMessageWrapper<T> messageWrapper;
    private MqEvent mqEvent;

    public static <T> ListenerQueueEntity<T> build(Channel channel, Message message, MqMessageWrapper<T> messageWrapper
            , MqEvent mqEvent) {
        ListenerQueueEntity<T> entity = new ListenerQueueEntity<>();
        return entity.setChannel(channel).setMessage(message).setMessageWrapper(messageWrapper).setMqEvent(mqEvent);
    }

    public enum MqEvent {
        SEND_SUCCESS,
        SEND_FAILURE,
        CONSUMER_SUCCESS,
        CONSUMER_FAILURE
    }
}
