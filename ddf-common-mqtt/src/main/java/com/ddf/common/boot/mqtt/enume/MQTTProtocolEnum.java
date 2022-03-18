package com.ddf.common.boot.mqtt.enume;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * <p>mqtt 协议 枚举 </p >
 *
 * MQTT Broker一般都会支持多种协议，
 * mqtt
 * mqtts
 * ws
 * wss
 *
 * 作为服务端可以将所有协议的连接地址返回给客户端，让客户端选择性连接
 * 也应该允许客户端获取执行协议的连接地址，然后进行连接交互
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 12:01
 */
@Getter
public enum MQTTProtocolEnum {

    /**
     * mqtt_tcp
     */
    MQTT_TCP("mqtt_tcp"),

    /**
     * mqtt_http
     */
    MQTT_HTTP("mqtt_http"),

    /**
     * websocket
     */
    MQTT_WS("mqtt_ws"),

    /**
     * websockets
     */
    MQTT_WSS("mqtt_wss")
    ;

    private final String protocol;

    private static final Map<String, MQTTProtocolEnum> MAPPINGS;

    MQTTProtocolEnum(String protocol) {
        this.protocol = protocol;
    }

    static {
        MAPPINGS = Arrays.stream(MQTTProtocolEnum.values()).collect(Collectors.toMap(MQTTProtocolEnum::getProtocol, obj -> obj));
    }

    public MQTTProtocolEnum resolve(String protocol) {
        return MAPPINGS.get(protocol);
    }
}
