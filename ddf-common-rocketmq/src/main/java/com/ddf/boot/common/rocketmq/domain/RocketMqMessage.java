package com.ddf.boot.common.rocketmq.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

/**
 * 火箭mq消息
 *
 * @author YiMing
 * @since 2023/10/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RocketMqMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 5484128488254938570L;

    public static final String WRAPPER_BIZ_ID_SEPARATOR = "#";

    /**
     * 主题
     */
    private String topic;

    /**
     * 路由表达式
     */
    private String expression;

    /**
     * 消息体
     */
    private MessagePayload payLoad;

    /**
     * 延迟时间 (s)
     */
    private Long delayTime;


    public void check() {
        Assert.hasLength(getTopic(), "Topic不能为空");
        Assert.hasLength(getExpression(), "Expression不能为空");

        Assert.isTrue(Objects.nonNull(getPayLoad()), "PayLoad不能为空");
    }

}
