package com.ddf.common.boot.mqtt.model.response;

import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>连接信息响应对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/22 14:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ConnectionInfoResponse implements Serializable {

    private static final long serialVersionUID = 5616664493701130945L;

    /**
     * 协议
     * @see MQTTProtocolEnum#getProtocol()
     */
    private String protocol;

    /**
     * 连接地址
     * tcp://localhost:1883
     */
    private String url;

    /**
     * 原因
     */
    private String msg;
}
