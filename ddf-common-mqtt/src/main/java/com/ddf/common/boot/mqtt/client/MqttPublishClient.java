package com.ddf.common.boot.mqtt.client;

import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.boot.mqtt.model.request.MqttMessageRequest;
import com.ddf.common.boot.mqtt.model.request.MqttTopicCreateRequest;
import com.ddf.common.boot.mqtt.model.response.MqttTopicCreateResponse;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * <p>description</p >
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
     * 创建topic
     *
     * @param request
     * @return
     */
    public MqttTopicCreateResponse createTopic(MqttTopicCreateRequest request) {
        PreconditionUtil.requiredParamCheck(request);
        return MqttTopicCreateResponse.builder()
                .topicUrl(String.join(MqttTopic.TOPIC_LEVEL_SEPARATOR, request.getTopicType()
                        .getFullTopicPrefix(), request.getTopicId()))
                .build();
    }

    /**
     * 发布消息
     *
     * @param request
     */
    public <T> void publish(MqttMessageRequest<T> request) {
        mqttDefinition.publish(request);
    }
}
