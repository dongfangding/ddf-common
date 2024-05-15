package com.ddf.common.ons.console.model.response;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>根据Group查询死信返回列表</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/06/02 20:05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsoleOnsDLQMessagePageQueryByGroupResponse implements Serializable {

    private static final long serialVersionUID = 3606151399446161194L;

    /**
     * StoreSize
     */
    public Integer storeSize;

    /**
     * ReconsumeTimes
     */
    public Integer reconsumeTimes;

    /**
     * StoreTimestamp
     */
    public Long storeTimestamp;

    /**
     * InstanceId
     */
    public String instanceId;

    /**
     * MsgId
     */
    public String msgId;

    /**
     * StoreHost
     */
    public String storeHost;

    /**
     * Topic
     */
    public String topic;

    /**
     * BornTimestamp
     */
    public Long bornTimestamp;

    /**
     * BodyCRC
     */
    public Integer bodyCRC;

    /**
     * BornHost
     */
    public String bornHost;

    /**
     * REAL_TOPIC
     * api文档未描述含义， 但从数据看是RETRY+实例ID+GROUP
     */
    private String realTopic;

    /**
     * ORIGIN_MESSAGE_ID
     */
    private String originMessageId;

    /**
     * bizId
     */
    private String keys;

    /**
     * TAGS
     */
    private String tags;
}
