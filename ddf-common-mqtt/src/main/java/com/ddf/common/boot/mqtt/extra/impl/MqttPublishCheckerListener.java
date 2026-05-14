package com.ddf.common.boot.mqtt.extra.impl;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;
import com.ddf.common.boot.mqtt.extra.MqttPublishListener;
import com.ddf.common.boot.mqtt.model.request.InnerMqttMessageRequest;
import com.ddf.common.boot.mqtt.model.support.MqttMessagePayload;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.common.MqttMessage;

/**
 * 消息发送前置检查
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/21 13:10
 */
@RequiredArgsConstructor
@Slf4j
public class MqttPublishCheckerListener implements MqttPublishListener {

    private final EmqConnectionProperties emqConnectionProperties;

    // 令牌桶限流
    private static RateLimiter rateLimiter = RateLimiter.create(5000);

    @PostConstruct
    public void init() {
        // 初始化令牌桶限流, 先预热5秒钟，后续再恢复每秒令牌
        rateLimiter = RateLimiter.create(emqConnectionProperties.getPublishRateLimit(), Duration.ofSeconds(5));
    }

    /**
     * @param message 参数
     * @param payload 参数
     * @param request 请求对象
     */
    @Override
    public void beforePublish(MqttMessage message, MqttMessagePayload payload, InnerMqttMessageRequest request) {
        if (request.getTopic().startsWith("/")) {
            // 因为mqtt协议自身是使用/来作为层级分隔符的，如果开头也使用/, 会增加人工上的识别成本
            throw new IllegalArgumentException("topic must not start with /");
        }

        final Integer maxPayloadSize = emqConnectionProperties.getMaxPayloadSize();
        if (message.getPayload().length > maxPayloadSize) {
            log.error("mqtt消息payload大小超过最大限制： {}, msg = {}", maxPayloadSize, JsonUtil.toJson(request));
            throw new IllegalArgumentException("mqtt消息payload大小超过最大限制： " + maxPayloadSize);
        }

        // 限流检查
        if (!rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            log.error("mqtt发布消息限流，topic: {}, msg = {}", request.getTopic(), JsonUtil.toJson(request));
            throw new ServerErrorException(BaseErrorCallbackCode.REQUEST_TOO_MANY);
        }
    }

    /**
     * @param message 参数
     * @param payload 参数
     */
    @Override
    public void afterPublish(MqttMessage message, MqttMessagePayload payload) {

    }
}
