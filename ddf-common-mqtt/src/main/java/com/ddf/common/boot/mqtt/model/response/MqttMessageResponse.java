package com.ddf.common.boot.mqtt.model.response;

import com.ddf.common.boot.mqtt.model.support.header.ServerClientInfo;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>发送mqtt消息响应类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 20:25
 */
@Data
public class MqttMessageResponse implements Serializable {

    private ServerClientInfo serverInfo;
}
