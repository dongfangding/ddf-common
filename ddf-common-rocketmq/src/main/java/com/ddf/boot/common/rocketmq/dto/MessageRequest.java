package com.ddf.boot.common.rocketmq.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>发送消息的参数类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/09/15 20:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest<T> implements Serializable {

    private static final long serialVersionUID = 5252780995346856157L;

    /**
     * topic
     */
    private String topic;

    /**
     * 发送的过滤tag
     */
    private String tag;

    /**
     * 便于查找的业务唯一id
     */
    private String bizId;

    /**
     * 消息内容
     */
    private T body;

    /**
     * 消息延迟级别
     * @see RocketMQDelayTimeMapping
     * @see RocketMQDelayLevelMapping
     */
    private Integer level = 0;

    /**
     * 获取MQ发往的目的地， 遵循RocketMQ代码规范，由topic和tag组合成
     * @return
     */
    public String getDestination() {
        return String.join(":", topic, tag);
    }
}
