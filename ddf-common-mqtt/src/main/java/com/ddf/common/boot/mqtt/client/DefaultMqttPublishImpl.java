package com.ddf.common.boot.mqtt.client;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.api.util.MessagePackUtil;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;
import com.ddf.common.boot.mqtt.extra.MqttPublishListener;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqtt.model.response.MqttMessageResponse;
import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import com.ddf.common.boot.mqtt.model.support.MqttMessagePayload;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

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
    private final Map<String, MqttPublishListener> listenerMap;
    private final EmqConnectionProperties mqttProperties;

    public DefaultMqttPublishImpl(MqttClient mqttClient, Map<String, MqttPublishListener> listenerMap, EmqConnectionProperties mqttProperties    ) {
        this.mqttClient = mqttClient;
        this.listenerMap = listenerMap;
        this.mqttProperties = mqttProperties;
    }

    /**
     * 发布消息
     *
     * @param request
     */
    @Override
    public ResponseData<MqttMessageResponse> publish(InnerMqttMessageRequest request) {
        PreconditionUtil.requiredParamCheck(request);
        final MqttMessage message = new MqttMessage();
        final MqttMessageControl control = request.getControl();
        message.setId((int) IdsUtil.getNextLongId());
        message.setQos(control
                .getQos()
                .getQos());
        message.setRetained(control.getRetain());
        // 将请求对象转换为实际的mqtt message payload
        final MqttMessagePayload payload = MqttMessagePayload.fromMessageRequest(request, mqttClient.getClientId());
        final byte[] bytes = MessagePackUtil.writeValueAsBytes(payload);
        message.setPayload(bytes);

        // 预留的发送前置处理监听
        if (CollUtil.isNotEmpty(listenerMap)) {
            listenerMap.forEach((beanName, bean) -> {
                bean.beforePublish(message, payload, request);
            });
        }
        try {
            mqttClient.publish(request.getTopic(), message);
        } catch (MqttException e) {
            log.error("mqtt消息发送失败, 消息内容 = {}", JsonUtil.asString(request));
            return ResponseData.failure("mqtt_error", e.getMessage());
        }
        // 预留的发送成功处理监听
        if (CollUtil.isNotEmpty(listenerMap)) {
            listenerMap.forEach((beanName, bean) -> {
                bean.afterPublish(message, payload);
            });
        }
        final MqttMessageResponse messageResponse = new MqttMessageResponse();
        messageResponse.setServerInfo(payload.getServerInfo());
        return ResponseData.success(messageResponse);
    }
}
