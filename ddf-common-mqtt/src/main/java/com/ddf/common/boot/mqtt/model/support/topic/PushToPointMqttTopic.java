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
public class PushToPointMqttTopic implements MqttTopic {

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
                GlobalStorage.NOTICE_TOPIC, "2point");
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
        final String topicPrefix = getTopicPrefix();
        if (fullTopic.startsWith(topicPrefix)) {
            final String userId = StringUtils.remove(fullTopic, topicPrefix + GlobalStorage.TOPIC_SEPARATOR);
            final PushToPointMqttTopic topic = new PushToPointMqttTopic();
            topic.setUserId(userId);
        }
        return null;
    }
}
