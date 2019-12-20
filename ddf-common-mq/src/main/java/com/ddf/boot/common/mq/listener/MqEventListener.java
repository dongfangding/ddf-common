package com.ddf.boot.common.mq.listener;

import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * 消息在发送或者消费期间的事件
 * 准备做成一个默认实现，可以把要发送的消息异步落库（但不百分百保证），提供消息的状态跟踪；
 *
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
 * @date 2019/12/16 0016 17:48
 */
public interface MqEventListener {


    /**
     * 消息发送成功的回调事件
     *
     * @param queueDefinition
     * @param messageWrapper
     * @return void
     * @author dongfang.ding
     * @date 2019/12/16 0016 17:50
     **/
    default <T> void sendSuccess(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper) {

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
    default <T> void sendFailure(QueueBuilder.QueueDefinition queueDefinition, MqMessageWrapper<T> messageWrapper, Throwable throwable) {

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
    default <T> void consumerSuccess(RabbitListener rabbitListener, MqMessageWrapper<T> messageWrapper) {

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
    default <T> void consumerFailure(RabbitListener rabbitListener, MqMessageWrapper<T> messageWrapper, Throwable throwable) {

    }
}
