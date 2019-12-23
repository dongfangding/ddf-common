package com.ddf.boot.common.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * fixme
 * 消息发送前可以更改消息，现在自定义RabbitTemplate会报错，暂未解决。如果解决可以在这里统一拦截处理记录发送的数据，这样就不用
 * 在RabbitTemplateHelper中的每个发送方法中都去调用发送事件了
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
 * @date 2019/12/10 0010 19:57
 */
@Slf4j
@Component
public class CustomizeMessagePostProcessor implements MessagePostProcessor {
    /**
     * Change (or replace) the message.
     *
     * @param message the message.
     * @return the message.
     * @throws AmqpException an exception.
     */
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        String encoding = message.getMessageProperties().getContentEncoding();
        try {
            String body = new String(message.getBody(), encoding);
            log.debug("读取到要发送的消息内容为: {}", body);
        } catch (UnsupportedEncodingException e) {
            log.error("消息发送前置处理器处理失败", e);
        }
        return message;
    }
}
