package com.ddf.common.boot.mqtt.client;

import com.ddf.common.boot.mqtt.model.request.MqttMessageRequest;
import com.ddf.common.boot.mqtt.model.support.body.MessageBody;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 16:22
 */
public interface MqttDefinition {

    /**
     * 发布消息
     *
     * @param request
     */
    <T extends MessageBody> void publish(MqttMessageRequest<T> request);
}
