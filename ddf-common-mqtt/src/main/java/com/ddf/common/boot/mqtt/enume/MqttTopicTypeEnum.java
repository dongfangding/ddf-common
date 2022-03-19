package com.ddf.common.boot.mqtt.enume;

import com.ddf.common.boot.mqtt.define.MqttTopicType;
import com.ddf.common.boot.mqtt.support.GlobalStorage;
import lombok.Getter;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * <p>topic类型</p >
 * 使用类来标识topic的意义在于客户端要使用大量的topic, 还需要注意topic不能重复， 这是个不太好处理的
 * 工作， 另外可能topic还需要区分场景， 比如一些通知类消息， 一些私聊消息， 甚至群聊消息， 那么最好按照场景
 * 分配下固定的一些前缀， 让topic更具有辨识度，甚至可以做一些功能区分</p >
 *
 * 比如私聊topic c2c/1  c2c代表私聊， 1就是用户id
 * 比如群聊topic chatRoom/1  chatRoom代表群聊，1就是聊天室id
 * 业务只需要关心自己的数据， 如房间id, 用户id, 因为附加了不同的前缀，使用方就可以轻松避免topic重复问题
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 15:28
 */
public enum MqttTopicTypeEnum implements MqttTopicType {


    /**
     * topic类型，根据类型不同附加不同的前缀
     */
    PRIVATE_MESSAGE_C2C("im/c2c"),
    CHAT_ROOM_MESSAGE("im/chat_room"),
    PUSH_MESSAGE_2C("notice/2c"),
    CHAT_ROOM_PUSH_MESSAGE("notice/chat_room")

    ;

    @Getter
    private final String prefix;

    MqttTopicTypeEnum(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取topic完整前缀
     *
     * @return
     */
    @Override
    public String getFullTopicPrefix() {
        return String.join(MqttTopic.TOPIC_LEVEL_SEPARATOR, GlobalStorage.SYSTEM_CLIENT_ID_PREFIX, getPrefix());
    }
}
