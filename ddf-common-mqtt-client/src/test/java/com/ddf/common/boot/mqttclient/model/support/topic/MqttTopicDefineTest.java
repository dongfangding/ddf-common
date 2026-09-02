package com.ddf.common.boot.mqttclient.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MQTT topic 定义测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class MqttTopicDefineTest {

    @AfterEach
    void tearDown() {
        GlobalStorage.SYSTEM_CLIENT_ID_PREFIX = GlobalStorage.TOPIC_SEPARATOR + "DEFAULT_CLIENT_ID";
    }

    @Test
    @DisplayName("应生成并反解析私聊 topic")
    void shouldBuildAndParsePointToPointTopic() {
        GlobalStorage.SYSTEM_CLIENT_ID_PREFIX = "/APP";
        Im2PointMqttTopic topic = new Im2PointMqttTopic("user-1");

        String fullTopic = topic.getFullTopic();
        Im2PointMqttTopic parsed = (Im2PointMqttTopic) topic.convertTopicObj(fullTopic);

        assertEquals("/APP//POINT//PRIVATE_MESSAGE/user-1", fullTopic);
        assertEquals("user-1", parsed.getIdentityId());
    }

    @Test
    @DisplayName("应生成并反解析群聊 topic")
    void shouldBuildAndParseGroupTopic() {
        GlobalStorage.SYSTEM_CLIENT_ID_PREFIX = "/APP";
        ImChatRoomMqttTopic topic = new ImChatRoomMqttTopic("room-9");

        String fullTopic = topic.getFullTopic();
        ImChatRoomMqttTopic parsed = (ImChatRoomMqttTopic) topic.convertTopicObj(fullTopic);

        assertEquals("/APP//GROUP//CHAT_ROOM/room-9", fullTopic);
        assertEquals("room-9", parsed.getIdentityId());
    }

    @Test
    @DisplayName("应生成通知类 topic")
    void shouldBuildNoticeTopics() {
        GlobalStorage.SYSTEM_CLIENT_ID_PREFIX = "/APP";
        Notice2PointMqttTopic pointTopic = new Notice2PointMqttTopic("user-2");
        NoticeChatRoomMqttTopic roomTopic = new NoticeChatRoomMqttTopic("room-2");

        assertEquals("/APP//POINT//PRIVATE_MESSAGE/NOTICE/user-2", pointTopic.getFullTopic());
        assertEquals("/APP//POINT//PRIVATE_MESSAGE/NOTICE/room-2", roomTopic.getFullTopic());
    }
}
