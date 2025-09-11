package com.ddf.common.boot.mqtt.client;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.boot.mqtt.extra.MqttPublishListener;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import com.ddf.common.boot.mqtt.model.support.MqttMessagePayload;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

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

    public DefaultMqttPublishImpl(MqttClient mqttClient, Map<String, MqttPublishListener> listenerMap) {
        this.mqttClient = mqttClient;
        this.listenerMap = listenerMap;
    }

    /**
     * 发布消息
     *
     * @param request
     */
    @Override
    public void publish(InnerMqttMessageRequest request) {
        PreconditionUtil.requiredParamCheck(request);
        final MqttMessage message = new MqttMessage();
        final MqttMessageControl control = request.getControl();
        message.setId((int) IdsUtil.getNextLongId());
        message.setQos(control.getQos().getQos());
        message.setRetained(control.getRetain());

        // 将请求对象转换为实际的mqtt message payload
        final MqttMessagePayload payload = MqttMessagePayload.fromMessageRequest(request, mqttClient.getClientId());
        message.setPayload(JsonUtil.asString(payload).getBytes(StandardCharsets.UTF_8));

        // 预留的发送前置处理监听
        if (CollUtil.isNotEmpty(listenerMap)) {
            listenerMap.forEach((beanName, bean) -> {
                bean.beforePublish(message, payload);
            });
        }
        boolean result = false;
        try {
            mqttClient.publish(request.getTopic().startsWith("/") ? request.getTopic() : "/" + request.getTopic(), message);
            result = true;
        } catch (MqttException e) {
            // todo 返回对象告知失败
            log.error("mqtt消息发送失败, 消息内容 = {}", JsonUtil.asString(request));
        }
        // 预留的发送成功处理监听
        if (result && CollUtil.isNotEmpty(listenerMap)) {
            listenerMap.forEach((beanName, bean) -> {
                bean.afterPublish(message, payload);
            });
        }
    }
}
