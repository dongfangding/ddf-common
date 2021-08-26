package com.ddf.common.ons.mongodb;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ONS消息日志查询参数
 *
 * @author snowball
 * @date 2021/8/26 15:28
 **/
@Data
@NoArgsConstructor
public class OnsMessageLogQueryVO implements Serializable {

    private static final long serialVersionUID = -3051094455031519978L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 消息发送/消费状态, 取值{@link MessageStatusEnum}
     */
    private MessageStatusEnum messageStatus = MessageStatusEnum.FAILURE;

    public CollectionName getCollectionName() {
        return new CollectionName(messageStatus);
    }
}
