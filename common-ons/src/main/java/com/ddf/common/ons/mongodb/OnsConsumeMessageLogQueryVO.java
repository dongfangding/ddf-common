package com.ddf.common.ons.mongodb;

import java.io.Serializable;
import lombok.Data;

/**
 * ONS消息日志查询参数
 *
 * @author snowball
 * @date 2021/8/26 15:25
 **/
@Data
public class OnsConsumeMessageLogQueryVO extends OnsProduceMessageLogQueryVO implements Serializable {

    private static final long serialVersionUID = 1846889972434116819L;

    /**
     * 消费主机
     */
    private String consumeHost;

    /**
     * 消费者
     * 具体的消费类名
     */
    private String consumer;

    /**
     * GroupID
     */
    private String groupId;


}
