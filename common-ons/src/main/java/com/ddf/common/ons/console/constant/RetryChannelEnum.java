package com.ddf.common.ons.console.constant;

/**
 * <p>重试通道</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/19 16:27
 */
public enum RetryChannelEnum {

    /**
     * 重发指定 Message ID 的死信消息，使该消息能够被 Consumer 再次消费
     */
    ONS_DLQ_MESSAGE_RESEND_BY_ID,

    /**
     * 向指定的消费者推送消息
     */
    ONS_MESSAGE_PUSH
}
