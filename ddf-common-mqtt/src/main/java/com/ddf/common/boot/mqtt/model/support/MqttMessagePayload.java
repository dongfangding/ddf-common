package com.ddf.common.boot.mqtt.model.support;

import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.common.boot.mqtt.model.request.MqttMessageRequest;
import com.ddf.common.boot.mqtt.model.support.body.MessageBody;
import com.ddf.common.boot.mqtt.model.support.body.TextMessageBody;
import com.ddf.common.boot.mqtt.model.support.header.MqttHeader;
import com.ddf.common.boot.mqtt.model.support.header.ServerClientInfo;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * <p>发送的mqtt的实际消息对象， 该对象通过发送消息请求对象构建,舍弃了一些无必要参数，同时增加了一些自己作为服务端代码的一些参数</p >
 *
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 18:24
 */
@Data
public class MqttMessagePayload<T> implements Serializable {

    private static final long serialVersionUID = 1516322558409231083L;

    private MqttMessagePayload() {

    }

    /**
     * 基础请求头， 当然由于预留了扩展字段， 应该没有必要继承这个类继续扩展了，请使用扩展字段来存储自定义的字段
     */
    private MqttHeader header;

    /**
     * 服务端作为mqtt客户端时发送时附带的一些数据
     */
    private ServerClientInfo serverInfo;

    /**
     * 消息代码，  用来标识这个消息具体是做什么用的
     * 比如是用户上线通知、用户新消息通知，是一个消息的最小表示单位
     */
    private String messageCode;

    /**
     * 消息类型，比如文本、图片、视频等，用于一些消息的渲染处理， 此值留空，由使用方决定用处
     */
    private String contentType;

    /**
     * 反序列化类型，这个作为预留字段，调用方决定如何使用。整个请求对象在存储时是作为整个请求大对象的而持久化的，
     * 那么下面消息体的body字段持久化的时候就是消息对象的json序列化字符串。而取出来消息的要使用的时候是需要反序列化回来的，
     * 可以根据这个字段来判断来决定如何序列化，当然下面还有一个bizType字段是业务类型，根据场景决定可能也是可以使用的，
     * 使用方自己决定即可，这里只是预留字段
     *
     * @return
     */
    private String deserializeType;

    /**
     * 消息业务类型，这个类型大于消息代码， 标识某个业务下的消息，一个业务类型下面可以有很多消息类型
     *
     * 比如业务类型是某个群聊， 在群里发的消息类型有聊天文本， 有红包消息， 有送礼消息
     * 业务类型是一对一私聊，同样也存在消息代码时聊天文本、红包消息、送礼消息等
     */
    private String bizType;

    /**
     * 消息body
     *
     * 注意这个值来源于{@link MqttMessageRequest#getBody()}
     * 这里的T舍弃了限定符， 是为了避免对象序列化之后，由于多态无法反序列化问题。
     * 如果要支持，会把这一块搞发非常复杂，目前应该没有必要
     */
    private T body;

    /**
     * 通过外部发送消息的请求对象转换为实际要发送mqtt message的payload
     * {@link MqttMessage#setPayload(byte[])} ()}
     *
     * @param request
     * @param serverClientId
     * @param <T>
     * @return
     */
    public static <T extends MessageBody> MqttMessagePayload<T> fromMessageRequest(MqttMessageRequest<T> request, String serverClientId) {
        final MqttMessagePayload<T> payload = new MqttMessagePayload<>();
        payload.setHeader(request.getHeader());
        payload.setMessageCode(request.getMessageCode());
        payload.setContentType(request.getContentType());
        payload.setDeserializeType(request.getDeserializeType());
        payload.setBizType(request.getBizType());
        payload.setBody(request.getBody());

        final ServerClientInfo serverInfo = new ServerClientInfo();
        serverInfo.setClientId(serverClientId);
        serverInfo.setTimestamp(System.currentTimeMillis());
        payload.setServerInfo(serverInfo);

        return payload;
    }

    public static void main(String[] args) {
        final MqttMessageRequest<TextMessageBody> request = new MqttMessageRequest<>();
        final TextMessageBody body = new TextMessageBody();
        body.setMsg("haha");
        request.setBody(body);
        request.setMessageCode("didi");
        final MqttMessagePayload<TextMessageBody> payload = MqttMessagePayload.fromMessageRequest(request, "111");
        String str = JsonUtil.asString(payload);
        System.out.println("str = " + str);
        final MqttMessagePayload<TextMessageBody> request1 = JsonUtil.toBean(str, MqttMessagePayload.class);
        System.out.println("request1 = " + request1);
    }
}
