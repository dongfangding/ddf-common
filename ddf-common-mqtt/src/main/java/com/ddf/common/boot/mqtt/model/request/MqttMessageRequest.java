package com.ddf.common.boot.mqtt.model.request;

import com.ddf.common.boot.mqtt.model.support.BaseHeader;
import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import com.ddf.common.boot.mqtt.model.support.body.MessageBody;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * <p>发送mqtt消息请求类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 20:25
 */
@Data
public class MqttMessageRequest<T extends MessageBody> implements Serializable {

    /**
     * 基础请求头， 当然由于预留了扩展字段， 应该没有必要继承这个类继续扩展了，请使用扩展字段来存储自定义的字段
     */
    private BaseHeader header = BaseHeader.DEFAULT;

    /**
     * 控制mqtt消息行为参数
     */
    private MqttMessageControl control = MqttMessageControl.DEFAULT;

    /**
     * 接收端topic
     */
    @NotBlank(message = "topic不能为空")
    @Size(min = 1, max = 128, message = "topicId参数过长")
    private String topic;

    /**
     * 消息类型，  用来标识这个消息具体是做什么用的
     */
    @NotBlank(message = "topic不能为空")
    @Size(min = 1, max = 128, message = "topicId参数过长")
    private String messageType;

    /**
     * 消息业务类型，这个类型大于消息类型， 某个业务下的消息，一个业务类型下面可以有很多消息类型
     *
     * 比如业务类型是某个群聊， 在群里发的消息类型有聊天文本， 红包
     */
    @Size(min = 1, max = 128, message = "topicId参数过长")
    private String bizType;

    /**
     * 消息body
     */
    private T body;

}
