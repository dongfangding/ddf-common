package com.ddf.common.boot.mqtt.model.response;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 15:47
 */
@Data
@Builder
public class MqttTopicCreateResponse implements Serializable {

    private static final long serialVersionUID = -4975529829966473617L;

    /**
     * 完整的topic
     */
    private String topicUrl;

}
