package com.ddf.boot.common.mq.definition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * mq发送消息的统一格式类
 *
 * @author dongfang.ding
 * @date 2019/8/1 15:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqMessageWrapper<T> implements Serializable {
    private static final long serialVersionUID = -8328345290360094049L;

    /** 创建人 */
    private Long creator;

    /** 创建时间 */
    private Long createTime;

    /** 消息的唯一标识符 */
    private String messageId;
    /**
     * 当前重试次数
     */
    private int retryTimes;

    /** 序列化后的消息正文 */
    private T body;
}
