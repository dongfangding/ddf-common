package com.ddf.boot.common.rocketmq.domain;

import java.time.LocalDateTime;
import lombok.Data;


/**
 * 消息负载
 *
 * @author YiMing
 * @date 2023/10/09
 */
@Data
public class MessagePayload<T> {
    /**
     * 业务键，用于RocketMQ控制台查看消费情况
     */
    private String messageId;

    /**
     * 发送消息来源，用于排查问题
     */
    private String source = "";

    /**
     * 发送时间
     */
    private String sendTime = LocalDateTime.now().toString();

    /**
     * 重试次数，用于判断重试次数，超过重试次数发送异常警告
     */
    private Integer retryTimes = 0;

    /**
     * 数据
     */
    private T data;

    public static <T> MessagePayload<T> newInstance() {
        MessagePayload<T> payload = new MessagePayload<>();
        return payload;
    }
}
