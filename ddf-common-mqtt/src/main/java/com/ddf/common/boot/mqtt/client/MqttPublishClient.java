package com.ddf.common.boot.mqtt.client;

import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqtt.model.response.MqttMessageResponse;

/**
 * <p>对外暴露的的Client工具</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/19 15:24
 */
public class MqttPublishClient {
    private final MqttDefinition mqttDefinition;

    public MqttPublishClient(MqttDefinition mqttDefinition) {
        this.mqttDefinition = mqttDefinition;
    }

    /**
     * 发布消息
     *
     * @param request 请求对象
     */
    public ResponseData<MqttMessageResponse> publish(InnerMqttMessageRequest request) {
        return mqttDefinition.publish(request);
    }
}
