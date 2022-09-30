package com.ddf.common.boot.mqtt.model.support.body;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>预定义的一个简单的发送文本的消息body对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/20 13:14
 */
@Data
public class TextMessageBody extends AbstractMessageBody implements Serializable {
    private static final long serialVersionUID = 4140157051382161101L;
}
