package com.ddf.common.boot.mqtt.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>对点聊天topic格式</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 12:30
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Im2PointMqttTopic implements MqttTopicDefine, Serializable {

    private static final long serialVersionUID = 5668777228627540185L;

    /**
     * 身份id
     */
    private String identityId;

    /**
     * topic前缀
     *
     * @return
     */
    public static String getTopicPrefix() {
        return String.join(GlobalStorage.TOPIC_SEPARATOR, GlobalStorage.SYSTEM_CLIENT_ID_PREFIX,
                GlobalStorage.IM_TOPIC, GlobalStorage.PRIVATE_MESSAGE_TOPIC);
    }

    @Override
    public String identityId() {
        return identityId;
    }

    /**
     * 获取完整的topic
     *
     * @return
     */
    @Override
    public String getFullTopic() {
        return String.join(GlobalStorage.TOPIC_SEPARATOR, getTopicPrefix(), identityId);
    }

    /**
     * 按照规则通过完整的topic路径反解析成对应的topic对象
     *
     * @param fullTopic
     * @return
     */
    @Override
    public <T> T convertTopicObj(String fullTopic) {
        final String topicPrefix = getTopicPrefix();
        if (fullTopic.startsWith(topicPrefix)) {
            final String identityId = StringUtils.remove(fullTopic, topicPrefix + GlobalStorage.TOPIC_SEPARATOR);
            final Im2PointMqttTopic topic = new Im2PointMqttTopic();
            topic.setIdentityId(identityId);
        }
        return null;
    }
}
