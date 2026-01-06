package com.ddf.common.boot.mqtt.extra;

import com.ddf.common.boot.mqtt.model.support.MqttMessagePayload;
import org.eclipse.paho.mqttv5.common.MqttMessage;

/**
 * <p>消息发送事件监听</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/21 13:10
 */
public interface MqttPublishListener {

    /**
     * 消息发送前, 注意，实现的时候注意开启异步，否则会阻塞消息发送
     *
     * @param message 已经构建好的MqttMessage对象
     * @param payload 携带的数据保温
     */
    void beforePublish(MqttMessage message, MqttMessagePayload payload);

    /**
     * 消息发送完成后时间，可以做消息落地业务, 注意，实现的时候注意开启异步，否则会阻塞消息发送
     *
     * @param message 已经构建好的MqttMessage对象
     * @param payload 携带的数据保温
     */
    void afterPublish(MqttMessage message, MqttMessagePayload payload);
}
