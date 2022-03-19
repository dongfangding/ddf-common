package com.ddf.common.boot.mqtt.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>一对一聊天topic格式</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 12:30
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivateMessageMqttTopic implements MqttTopic, Serializable {

    private static final long serialVersionUID = 5668777228627540185L;

    /**
     * 用户uid
     */
    private String userId;

    /**
     * topic前缀
     *
     * @return
     */
    public static String getTopicPrefix() {
        return String.join(GlobalStorage.TOPIC_SEPARATOR, GlobalStorage.SYSTEM_CLIENT_ID_PREFIX,
                GlobalStorage.IM_TOPIC, "c2c");
    }

    /**
     * 获取完整的topic
     *
     * @return
     */
    @Override
    public String getFullTopic() {
        return String.join(GlobalStorage.TOPIC_SEPARATOR, getTopicPrefix(), userId);
    }

    /**
     * 按照规则通过完整的topic路径反解析成对应的topic对象
     *
     * @param fullTopic
     * @return
     */
    @Override
    public <T> T convertTopicObj(String fullTopic) {
        return null;
    }
}
