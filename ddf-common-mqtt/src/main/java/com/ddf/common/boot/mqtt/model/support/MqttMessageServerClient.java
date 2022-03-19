package com.ddf.common.boot.mqtt.model.support;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>服务端作为mqtt客户端时发送时附带的一些数据</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 21:32
 */
@Data
public class MqttMessageServerClient implements Serializable {

    /**
     * 服务端作为mqtt客户端时使用的clientId
     */
    private String clientId;

    /**
     * 发送时的时间戳
     */
    private Long timestamp;
}
