package com.ddf.common.boot.mqtt.model.request;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 20:25
 */
@Data
public class MqttMessageRequest implements Serializable {

    private String messageId;

}
