package com.ddf.common.boot.mqtt.exception;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import lombok.Getter;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 14:37
 */
public enum MqttCallbackCode implements BaseCallbackCode {

    /**
     * mqtt 错误码
     */
    MQTT_CONFIG_CONNECTION_CLIENT_MISS("mqtt0001", "mqtt 连接未配置client属性"),
    MQTT_CONFIG_CONNECTION_TCP_PROTOCOL_ERROR("mqtt0002", "mqtt 连接为配置tcp协议地址"),
    MQTT_CONFIG_CREATE_CLIENT_ERROR("mqtt0001", "mqtt 创建客户端错误"),
    MQTT_CONFIG_CONNECTION_ERROR("mqtt0001", "mqtt 连接配置错误")

    ;


    MqttCallbackCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Getter
    private final String code;

    @Getter
    private final String description;
}
