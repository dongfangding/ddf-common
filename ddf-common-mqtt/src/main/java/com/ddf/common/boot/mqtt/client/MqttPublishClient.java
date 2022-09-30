package com.ddf.common.boot.mqtt.client;

import com.ddf.common.boot.mqtt.model.request.MqttMessageRequest;
import com.ddf.common.boot.mqtt.model.support.body.MessageBody;

/**
 * <p>对外暴露的的Client工具</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 15:24
 */
public class MqttPublishClient {

    private final MqttDefinition mqttDefinition;


    public MqttPublishClient(MqttDefinition mqttDefinition) {
        this.mqttDefinition = mqttDefinition;
    }

    /**
     * 发布消息
     *
     * @param request
     */
    public <T extends MessageBody> void publish(MqttMessageRequest<T> request) {
        mqttDefinition.publish(request);
    }
}
