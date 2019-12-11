package com.ddf.boot.common.mq.config;

import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 自定义封装的一些mq方法全局属性
 *
 * @author dongfang.ding
 * @date 2019/12/11 0011 10:30
 */
@Data
@Component
@ConfigurationProperties(prefix = "customs.mq-message-properties")
public class MqMessageProperties {

    /**
     * 最大重投次数
     *
     * @see com.ddf.boot.common.mq.helper.RabbitTemplateHelper#nackAndRequeueIfFailure(Channel, Message, QueueBuilder.QueueDefinition, MqMessageWrapper, Consumer)
     */
    private int maxRequeueTimes = 5;
}
