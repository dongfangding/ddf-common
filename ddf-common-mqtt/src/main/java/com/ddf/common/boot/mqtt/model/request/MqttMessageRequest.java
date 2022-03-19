package com.ddf.common.boot.mqtt.model.request;

import com.ddf.common.boot.mqtt.model.support.BaseHeader;
import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>发送mqtt消息请求类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 20:25
 */
@Data
public class MqttMessageRequest<T> implements Serializable {

    /**
     * 基础请求头， 当然由于预留了扩展字段， 应该没有必要继承这个类继续扩展了，请使用扩展字段来存储自定义的字段
     */
    private BaseHeader baseHeader;

    /**
     * 控制mqtt消息行为参数
     */
    private MqttMessageControl control = MqttMessageControl.DEFAULT;

    /**
     * 接收端topic
     */
    private String topic;

    /**
     * 消息body
     */
    private T body;

}
