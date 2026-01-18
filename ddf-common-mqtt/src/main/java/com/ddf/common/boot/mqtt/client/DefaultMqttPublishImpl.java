package com.ddf.common.boot.mqtt.client;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.api.util.MessagePackUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;
import com.ddf.common.boot.mqtt.enume.MqttQosEnum;
import com.ddf.common.boot.mqtt.extra.MqttPublishListener;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqtt.model.response.MqttMessageResponse;
import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import com.ddf.common.boot.mqtt.model.support.MqttMessagePayload;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * MQTT 消息发布实现
 * <p>
 * 优化说明：
 * 1. 移除 SpringContextHolder.getBean() 调用，改为构造函数注入，消除性能损耗
 * 2. 预先构建 QoS 到线程池的映射，O(1) 时间复杂度获取
 * 3. 添加 RetryTemplate 重试机制，提高消息发送可靠性
 * </p>
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 15:50
 */
@Slf4j
public class DefaultMqttPublishImpl implements MqttDefinition {

    /**
     * QoS 等级到线程池的映射，预先构建消除运行时查找
     */
    private final Map<MqttQosEnum, ThreadPoolTaskExecutor> qosExecutors;

    private final MqttClient mqttClient;
    private final Map<String, MqttPublishListener> listenerMap;
    private final EmqConnectionProperties mqttProperties;
    private final RetryTemplate retryTemplate;

    public DefaultMqttPublishImpl(
            MqttClient mqttClient,
            Map<String, MqttPublishListener> listenerMap,
            EmqConnectionProperties mqttProperties,
            Map<MqttQosEnum, ThreadPoolTaskExecutor> qosExecutors,
            RetryTemplate retryTemplate) {
        this.mqttClient = mqttClient;
        this.listenerMap = listenerMap;
        this.mqttProperties = mqttProperties;
        this.qosExecutors = qosExecutors;
        this.retryTemplate = retryTemplate;
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
        message.setQos(control.getQos().getQos());
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
        final MqttMessageResponse messageResponse = new MqttMessageResponse();
        messageResponse.setServerInfo(payload.getServerInfo());
        messageResponse.setAsync(control.getAsync());
        if (control.getAsync()) {
            ThreadPoolTaskExecutor executor = qosExecutors.get(control.getQos());
            if (executor == null) {
                log.warn("MQTT {} 线程池不存在，使用同步发送", control.getQos());
                return publishSync(request, message, payload, messageResponse);
            }
            executor.execute(() -> {
                try {
                    retryTemplate.execute(ctx -> {
                        mqttClient.publish(request.getTopic(), message);
                        return null;
                    });
                } catch (MqttException e) {
                    log.error("MQTT异步消息发送失败, topic={}, message={}", request.getTopic(), JsonUtil.asString(request), e);
                }
            });
            return ResponseData.success(messageResponse);
        }
        return publishSync(request, message, payload, messageResponse);
    }

    /**
     * 同步发送消息
     */
    private ResponseData<MqttMessageResponse> publishSync(InnerMqttMessageRequest request,
                                                           MqttMessage message,
                                                           MqttMessagePayload payload,
                                                           MqttMessageResponse messageResponse) {
        try {
            retryTemplate.execute(ctx -> {
                mqttClient.publish(request.getTopic(), message);
                return null;
            });
        } catch (MqttException e) {
            log.error("MQTT同步消息发送失败, topic={}, message={}", request.getTopic(), JsonUtil.asString(request), e);
            return ResponseData.failure("mqtt_error", e.getMessage());
        }
        // 预留的发送成功处理监听
        if (CollUtil.isNotEmpty(listenerMap)) {
            listenerMap.forEach((beanName, bean) -> {
                bean.afterPublish(message, payload);
            });
        }
        return ResponseData.success(messageResponse);
    }
}
