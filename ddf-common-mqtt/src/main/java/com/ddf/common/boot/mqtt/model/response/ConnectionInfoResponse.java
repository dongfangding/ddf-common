package com.ddf.common.boot.mqtt.model.response;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>连接信息响应对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/22 14:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ConnectionInfoResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 5616664493701130945L;

    /**
     * 协议，参见 {@code MQTTProtocolEnum#getProtocol()}
     */
    private String protocol;

    /**
     * 连接地址
     * tcp://localhost:1883
     */
    private String url;
}
