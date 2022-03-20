package com.ddf.common.boot.mqtt.client;

import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.boot.mqtt.model.request.MqttMessageRequest;
import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import com.ddf.common.boot.mqtt.model.support.MqttMessagePayload;
import com.ddf.common.boot.mqtt.model.support.body.MessageBody;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 15:50
 */
@Slf4j
public class DefaultMqttPublishImpl implements MqttDefinition {

    private final MqttClient mqttClient;

    public DefaultMqttPublishImpl(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    /**
     * 发布消息
     *
     * @param request
     */
    @Override
    public <T extends MessageBody> void publish(MqttMessageRequest<T> request) {
        PreconditionUtil.requiredParamCheck(request);
        final MqttMessage message = new MqttMessage();
        final MqttMessageControl control = request.getControl();
        message.setId((int) IdsUtil.getNextLongId());
        message.setQos(control.getQos().getQos());
        message.setRetained(control.getRetain());

        // 将请求对象转换为实际的mqtt message payload
        final MqttMessagePayload<T> payload = MqttMessagePayload.fromMessageRequest(request, mqttClient.getClientId());
        message.setPayload(JsonUtil.asString(payload).getBytes(StandardCharsets.UTF_8));
        try {
            mqttClient.publish(request.getTopic(), message);
        } catch (MqttException e) {
            log.error("mqtt消息发送失败, 消息内容 = {}", JsonUtil.asString(request));
        }
    }
}
