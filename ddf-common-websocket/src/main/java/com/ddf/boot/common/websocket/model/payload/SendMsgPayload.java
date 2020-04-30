package com.ddf.boot.common.websocket.model.payload;

import com.ddf.boot.common.websocket.enumerate.SendMsgTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 发送消息的数据主题lei$
 *
 * @author dongfang.ding
 * @date 2020/2/21 0021 12:46
 */
@Data
@Accessors(chain = true)
public class SendMsgPayload {

    /**
     * 消息类型
     */
    private SendMsgTypeEnum type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息目的地
     */
    private String to;

    /**
     * 消息发送方
     */
    private String from;
}
