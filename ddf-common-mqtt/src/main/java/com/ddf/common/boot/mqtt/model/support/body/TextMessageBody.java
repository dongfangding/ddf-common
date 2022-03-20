package com.ddf.common.boot.mqtt.model.support.body;

import lombok.Data;

/**
 * <p>预定义的一个简单的发送文本的消息body对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/20 13:14
 */
@Data
public class TextMessageBody implements MessageBody {

    /**
     * 文本内容
     */
    private String msg;
}
