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
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
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
 * @since 2022/03/19 15:50
 */
@Slf4j
public class DefaultMqttPublishImpl implements MqttDefinition {

    /**
     * QoS 等级到线程池的映射，预先构建消除运行时查找
     * @param mqttAsyncClient 参数
     * @param listenerMap 参数
     * @param emqConnectionProperties 参数
     * @param qosExecutors 参数
     * @param retryTemplate 参数
     */
    private final Map<MqttQosEnum, ThreadPoolTaskExecutor> qosExecutors;

    private final MqttAsyncClient mqttAsyncClient;
    private final Map<String, MqttPublishListener> listenerMap;
    private final EmqConnectionProperties emqConnectionProperties;
    private final RetryTemplate retryTemplate;

    /**
     * MQTT 缓冲区已满错误码
     * @see <a href="https://github.com/eclipse/paho.mqttv5.client/blob/master/org.eclipse.paho.client.mqttv5/src/main/java/org/eclipse/paho/mqttv5/common/packet/MqttProperties.java">MqttReasonCode</a>
     */
    private static final int MQTT_REASON_BUFFER_FULL = 32202;
    public DefaultMqttPublishImpl(
            MqttAsyncClient mqttAsyncClient,
            Map<String, MqttPublishListener> listenerMap,
            EmqConnectionProperties emqConnectionProperties,
            Map<MqttQosEnum, ThreadPoolTaskExecutor> qosExecutors,
            RetryTemplate retryTemplate) {
        this.mqttAsyncClient = mqttAsyncClient;
        this.listenerMap = listenerMap;
        this.emqConnectionProperties = emqConnectionProperties;
        this.qosExecutors = qosExecutors;
        this.retryTemplate = retryTemplate;
    }

    /**
     * 发布消息
     *
     * @param request 请求对象
     */
    @Override
    public ResponseData<MqttMessageResponse> publish(InnerMqttMessageRequest request) {
        PreconditionUtil.requiredParamCheck(request);
        final MqttMessage message = new MqttMessage();
        final MqttMessageControl control = request.getControl();
        message.setQos(control.getQos().getQos());
        message.setRetained(control.getRetain());
        // 将请求对象转换为实际的mqtt message payload
        final MqttMessagePayload payload = MqttMessagePayload.fromMessageRequest(request, mqttAsyncClient.getClientId());
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
                        mqttAsyncClient.publish(request.getTopic(), message);
                        return null;
                    });
                    // 异步发送成功后触发监听器
                    if (CollUtil.isNotEmpty(listenerMap)) {
                        listenerMap.forEach((beanName, bean) -> {
                            bean.afterPublish(message, payload);
                        });
                    }
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
     * @param request 请求对象
     * @param message 参数
     * @param payload 参数
     * @param messageResponse 参数
     */
    private ResponseData<MqttMessageResponse> publishSync(InnerMqttMessageRequest request,
                                                           MqttMessage message,
                                                           MqttMessagePayload payload,
                                                           MqttMessageResponse messageResponse) {
        try {
            final IMqttToken mqttToken = mqttAsyncClient.publish(request.getTopic(), message);
            // mqttAsyncClient不会关心实际结果
            mqttToken.setActionCallback(new MqttActionListener() {
                /**
                 * @param asyncActionToken 参数
                 */
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // 只有成功收到 PUBACK (QoS > 1) 才会进这里
                    log.debug("消息发送成功: {}", request.getTopic());
                }
                /**
                 * @param asyncActionToken 参数
                 * @param exception 参数
                 */
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    log.error("消息发送失败: request = {}, {}", request, exception.getMessage());
                }
            });
        } catch (MqttException mqttException) {
            int reasonCode = mqttException.getReasonCode();
            // org.eclipse.paho.mqttv5.common.MqttException.getMessage
            // 这个错误码，是发送的消息未确认的过多，就会报这个错，这种就不要重试了
            if (reasonCode == MQTT_REASON_BUFFER_FULL) {
                log.error("MQTT缓冲区已满，放弃当前发送重试，topic={}", request.getTopic());
                return ResponseData.failure("mqtt_congestion", "发送缓冲区已满");
            }
            log.error("MQTT同步消息发送失败, topic={}, message={}", request.getTopic(), JsonUtil.asString(request), mqttException);
            return ResponseData.failure("mqtt_error", mqttException.getMessage());
        }
        // 预留的发送成功处理监听
        listenerMap.forEach((beanName, bean) -> {
            bean.afterPublish(message, payload);
        });
        return ResponseData.success(messageResponse);
    }
}
