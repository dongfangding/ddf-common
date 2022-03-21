package com.ddf.common.boot.mqtt.model.request;

import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import com.ddf.common.boot.mqtt.model.support.body.MessageBody;
import com.ddf.common.boot.mqtt.model.support.header.MqttHeader;
import com.ddf.common.boot.mqtt.model.support.topic.MqttTopic;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    private MqttHeader header = MqttHeader.DEFAULT;

    /**
     * 控制mqtt消息行为参数
     */
    private MqttMessageControl control = MqttMessageControl.DEFAULT;

    /**
     * 接收端topic
     */
    @NotNull(message = "topic不能为空")
    private MqttTopic topic;

    /**
     * 消息代码，  用来标识这个消息具体是做什么用的
     * 比如是用户上线通知、用户新消息通知，是一个消息的最小表示单位
     */
    @NotBlank(message = "messageCode不能为空")
    @Size(min = 1, max = 128, message = "messageCode参数过长")
    private String messageCode;

    /**
     * 消息业务类型，这个类型大于消息代码， 标识某个业务下的消息，一个业务类型下面可以有很多消息类型
     *
     * 比如业务类型是某个群聊， 在群里发的消息类型有聊天文本， 有红包消息， 有送礼消息
     * 业务类型是一对一私聊，同样也存在消息代码时聊天文本、红包消息、送礼消息等
     */
    @Size(min = 1, max = 128, message = "messageCode参数过长")
    private String bizType;

    /**
     * 消息body
     */
    private T body;

}
