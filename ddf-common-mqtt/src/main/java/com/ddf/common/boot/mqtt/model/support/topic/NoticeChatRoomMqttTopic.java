package com.ddf.common.boot.mqtt.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>群聊推送通知topic格式</p >
 *
 * 与聊天的区别是， 聊天是将消息追加到聊天区域中， 而通知类的则是一些提醒，当然最终作用还是看使用方的定义
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 12:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeChatRoomMqttTopic extends Notice2PointMqttTopic {

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
        return GlobalStorage.PRIVATE_MESSAGE_TOPIC + GlobalStorage.NOTICE_TOPIC;
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
        final NoticeChatRoomMqttTopic topic = new NoticeChatRoomMqttTopic();
        if (fullTopic.startsWith(topicPrefix)) {
            final String identityId = StringUtils.remove(fullTopic, topicPrefix + GlobalStorage.TOPIC_SEPARATOR);
            topic.setRoomId(identityId);
        }
        return topic;
    }
}
