package com.ddf.boot.common.mq.definition;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.Message;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * mq发送消息的统一格式类
 *
 * @author dongfang.ding
 * @date 2019/8/1 15:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqMessageWrapper<T> implements Serializable {
    private static final long serialVersionUID = -8328345290360094049L;

    /** 创建人 */
    private Long creator;

    /** 创建时间 */
    private Long createTime;

    /** 消息的唯一标识符 */
    private String messageId;
    /**
     * 当前重投次数， 消息消费失败后提供一种重投机制，会将消息重新发送到队尾，如此往复；
     *
     * @see com.ddf.boot.common.mq.helper.RabbitTemplateHelper#nackAndRequeueIfFailure(Channel, Message, QueueBuilder.QueueDefinition, MqMessageWrapper, Consumer)
     */
    private int requeueTimes;

    /** 序列化后的消息正文 */
    private T body;
}
