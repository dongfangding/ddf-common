package com.ddf.common.boot.mqttclient.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;
import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>群聊topic格式</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/19 12:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImChatRoomMqttTopic extends AbstractGroupMqttTopic {

    @Serial
    private static final long serialVersionUID = 5668777228627540185L;

    /**
     * 身份id
     */
    private String roomId;

    @Override
    public String getIdentityId() {
        return roomId;
    }

    @Override
    public String getBizTopicPrefix() {
        return GlobalStorage.CHAT_ROOM_MESSAGE_TOPIC;
    }

    /**
     * 按照规则通过完整的topic路径反解析成对应的topic对象
     *
     * @param fullTopic full主题参数
     */
    @Override
    public MqttTopicDefine convertTopicObj(String fullTopic) {
        final String topicPrefix = getTopicPrefix();
        final ImChatRoomMqttTopic topic = new ImChatRoomMqttTopic();
        if (fullTopic.startsWith(topicPrefix)) {
            final String identityId = StringUtils.remove(fullTopic, topicPrefix + GlobalStorage.TOPIC_SEPARATOR);
            topic.setRoomId(identityId);
        }
        return topic;
    }
}
