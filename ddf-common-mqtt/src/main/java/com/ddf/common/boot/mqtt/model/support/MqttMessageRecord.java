package com.ddf.common.boot.mqtt.model.support;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/21 20:31
 */
@Data
public class MqttMessageRecord implements Serializable {
    static final long serialVersionUID = -8180023413025190817L;

    /**
     * id
     */
    private String id;

    /**
     * messageId
     */
    private String messageId;

    /**
     * 业务id
     */
    private String bizId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 消息代码
     */
    private String messageCode;

    /**
     * 调用端时间戳， 本模块代码在发送时也会传递一个时间戳
     */
    private Long sourceTimestamp;

    /**
     * 发送方身份id
     * 如用户id, 设备id，账号id，根据实际情况填写
     */
    private String sourceIdentityId;

    /**
     * 发送方身份名称
     */
    private String sourceIdentityName;

    /**
     * 发送方身份头像地址
     */
    private String sourceIdentityAvatarUrl;

    /**
     * 整个mqtt message的payload
     */
    private String payload;

    /**
     * 调用方自己设置的body
     */
    private String body;

    /**
     * 最终发送出去的topic
     */
    private String topicUrl;

    /**
     * 发送方身份id
     * 如用户id, 设备id，账号id，根据实际情况填写
     */
    private String targetIdentityId;

    /**
     * 发送时服务端时间
     */
    private String serverTimestamp;

    /**
     * 历史记录中是否显示该消息
     */
    private Boolean show;
}
