package com.ddf.common.boot.mqtt.model.support.header;

import com.ddf.common.boot.mqtt.enume.MqttQosEnum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * <p>mqtt 请求头</p >
 * <p>
 * 调用方可以在请求头里放入一些自己需要的数据，也可以放入一些唯一标识符之类的数据方便消息追溯
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 11:29
 */
@Data
public class MqttBaseHeader implements Serializable {

    private static final long serialVersionUID = 4813011310202804454L;

    public static MqttBaseHeader DEFAULT;

    static {
        DEFAULT = new MqttBaseHeader();
    }

    /**
     * 扩展字段
     */
    private Map<String, Object> extras = new HashMap<>(16);

    /**
     * 客户端可设置一个id用于追溯自己发送的消息
     */
    private String bizId;

    /**
     * 调用端时间戳， 本模块代码在发送时也会传递一个时间戳
     */
    private Long sourceTimestamp;

    /**
     * 发送方身份id
     * 如用户id, 设备id，账号id，根据实际情况填写, 比如群聊中，该消息的发送用户
     */
    private String senderId;

    /**
     * 发送方身份名称
     */
    private String senderName;

    /**
     * 发送方身份头像地址
     */
    private String senderAvatar;

    /**
     * 接收方身份id列表, 如果复用群组的话，可以在群组内通过该字段决定消息是指定哪些用户的
     */
    private List<String> receiverIds = new ArrayList<>();

    /**
     * 添加指定扩展字段的快速方法
     *
     * @param key
     * @param val
     * @return
     */
    public Map<String, Object> putExtra(String key, Object val) {
        this.extras.put(key, val);
        return extras;
    }

    /**
     * 获取指定扩展字段的快速方法
     *
     * @param key
     * @return
     */
    public Object getExtra(String key) {
        return this.extras.get(key);
    }
}
