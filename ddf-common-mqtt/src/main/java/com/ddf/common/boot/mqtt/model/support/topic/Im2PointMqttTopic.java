package com.ddf.common.boot.mqtt.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;
import lombok.AllArgsConstructor;
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
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Im2PointMqttTopic extends AbstractPoint2PointTopic {

    private static final long serialVersionUID = 5668777228627540185L;

    /**
     * 身份id
     */
    private String identityId;

    @Override
    public String getIdentityId() {
        return identityId;
    }

    @Override
    public String getBizTopicPrefix() {
        return GlobalStorage.PRIVATE_MESSAGE_TOPIC;
    }

    /**
     * 按照规则通过完整的topic路径反解析成对应的topic对象
     *
     * @param fullTopic
     * @return
     */
    @Override
    public MqttTopicDefine convertTopicObj(String fullTopic) {
        final String topicPrefix = getTopicPrefix();
        final Im2PointMqttTopic topic = new Im2PointMqttTopic();
        if (fullTopic.startsWith(topicPrefix)) {
            final String identityId = StringUtils.remove(fullTopic, topicPrefix + GlobalStorage.TOPIC_SEPARATOR);
            topic.setIdentityId(identityId);
        }
        return topic;
    }
}
