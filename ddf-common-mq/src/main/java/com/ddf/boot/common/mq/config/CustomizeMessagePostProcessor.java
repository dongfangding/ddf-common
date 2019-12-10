package com.ddf.boot.common.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * 消息发送前可以更改消息，暂时还没想到用处
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
            log.info("读取到要发送的消息内容为: {}", body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return message;
    }
}
