package com.ddf.common.boot.mqtt.model.support.header;

import com.ddf.common.boot.mqtt.enume.MqttQosEnum;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>mqtt 请求头， 实际发送到报文中的， 可能会附加一些额外的字段</p >
 *
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/19 11:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MqttHeaderMsg extends MqttBaseHeader implements Serializable {

    /**
     * 消息质量，请参考mqtt协议qos的设计含义
     * @see MqttQosEnum
     */
    private Integer qos;

    /**
     * 是否设置为保留消息，请参考mqtt协议保留消息的设计含义
     * https://www.emqx.io/docs/zh/v4.4/advanced/retained.html
     */
    private Boolean retain = Boolean.FALSE;

    /**
     * 历史记录中是否显示该消息
     */
    private Boolean show = Boolean.FALSE;

    /**
     * 发送方是否接收该消息
     */
    private Boolean includeSender = Boolean.FALSE;
}
