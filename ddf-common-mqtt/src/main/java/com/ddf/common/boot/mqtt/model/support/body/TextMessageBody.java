package com.ddf.common.boot.mqtt.model.support.body;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>预定义的一个简单的发送文本的消息body对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/20 13:14
 */
@Data
public class TextMessageBody extends AbstractMessageBody implements Serializable {
    @Serial
    private static final long serialVersionUID = 8362001043321300029L;
}
