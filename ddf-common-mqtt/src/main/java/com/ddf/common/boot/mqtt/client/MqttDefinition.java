package com.ddf.common.boot.mqtt.client;

import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqtt.model.response.MqttMessageResponse;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/19 16:22
 */
public interface MqttDefinition {

    /**
     * 发布消息
     *
     * @param request
     */
    ResponseData<MqttMessageResponse> publish(InnerMqttMessageRequest request);
}
