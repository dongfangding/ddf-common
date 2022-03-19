package com.ddf.common.boot.mqtt.model.support;

import com.ddf.common.boot.mqtt.model.request.MqttMessageRequest;
import lombok.Data;

/**
 * <p>发送的mqtt的实际消息对象， 该对象通过发送消息请求对象构建,舍弃了一些无必要参数，同时增加了一些自己作为服务端代码的一些参数</p >
 *
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 18:24
 */
@Data
public class MqttMessagePayload<T> {

    private MqttMessagePayload() {

    }

    /**
     * 基础请求头， 当然由于预留了扩展字段， 应该没有必要继承这个类继续扩展了，请使用扩展字段来存储自定义的字段
     */
    private BaseHeader header;

    /**
     * 服务端作为mqtt客户端时发送时附带的一些数据
     */
    private MqttMessageServerClient serverInfo;

    /**
     * 接收端topic
     */
    private String topic;

    /**
     * 消息业务类型，这个类型大于消息类型， 某个业务下的消息，一个业务类型下面可以有很多消息类型
     *
     * 比如业务类型是某个群聊， 在群里发的消息类型有聊天文本， 红包
     */
    private String bizType;

    /**
     * 消息类型，  用来标识这个消息具体是做什么用的
     */
    private String messageType;

    /**
     * 消息body
     */
    private T body;

    public static <T> MqttMessagePayload<T> fromMessageRequest(MqttMessageRequest<T> request, String serverClientId) {
        final MqttMessagePayload<T> payload = new MqttMessagePayload<>();
        payload.setHeader(request.getHeader());
        payload.setTopic(request.getTopic());
        payload.setMessageType(request.getMessageType());
        payload.setBizType(request.getBizType());
        payload.setBody(request.getBody());

        final MqttMessageServerClient serverInfo = new MqttMessageServerClient();
        serverInfo.setClientId(serverClientId);
        serverInfo.setTimestamp(System.currentTimeMillis());
        payload.setServerInfo(serverInfo);

        return payload;
    }
}
