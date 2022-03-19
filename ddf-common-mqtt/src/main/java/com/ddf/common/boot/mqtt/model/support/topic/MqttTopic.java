package com.ddf.common.boot.mqtt.model.support.topic;

import java.io.Serializable;

/**
 * <p>mqtt topic类， 使用类来标识topic的意义在于客户端要使用大量的topic, 还需要注意topic不能重复， 这是个不太好处理的
 * 工作， 另外可能topic还需要区分场景， 比如一些通知类消息， 一些私聊消息， 甚至群聊消息， 那么最好按照场景
 * 分配下固定的一些前缀， 让topic更具有辨识度，甚至可以做一些功能区分</p >
 *
 * 比如私聊topic c2c/1  c2c代表私聊， 1就是用户id
 * 比如群聊topic chatRoom/1  chatRoom代表群聊，1就是聊天室id
 * 业务只需要关心自己的数据， 如房间id, 用户id, 因为附加了不同的前缀，使用方就可以轻松避免topic重复问题
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 11:48
 */
public interface MqttTopic extends Serializable {

    /**
     * 获取完整的topic
     *
     * @return
     */
    String getFullTopic();

    /**
     * 按照规则通过完整的topic路径反解析成对应的topic对象
     *
     * @param fullTopic
     * @param <T>
     * @return
     */
    <T> T convertTopicObj(String fullTopic);

}
