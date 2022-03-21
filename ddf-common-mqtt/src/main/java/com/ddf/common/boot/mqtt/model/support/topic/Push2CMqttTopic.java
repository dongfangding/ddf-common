package com.ddf.common.boot.mqtt.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>对点推送topic格式</p >
 * 点对点
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 12:30
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Push2CMqttTopic implements MqttTopic {

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
                GlobalStorage.NOTICE_TOPIC, GlobalStorage.PRIVATE_MESSAGE_TOPIC);
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
            final Push2CMqttTopic topic = new Push2CMqttTopic();
            topic.setIdentityId(identityId);
        }
        return null;
    }
}
