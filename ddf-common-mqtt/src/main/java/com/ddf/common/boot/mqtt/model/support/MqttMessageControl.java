package com.ddf.common.boot.mqtt.model.support;

import com.ddf.common.boot.mqtt.enume.MqttQosEnum;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>mqtt 发送mqtt消息控制相关参数 </p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 11:45
 */
@Data
public class MqttMessageControl implements Serializable {

    private static final long serialVersionUID = -5649107898855362417L;

    public static MqttMessageControl DEFAULT;

    static {
        DEFAULT = new MqttMessageControl();
    }

    /**
     * 消息质量，请参考mqtt协议qos的设计含义
     */
    private MqttQosEnum qos = MqttQosEnum.AT_LAST_ONCE;

    /**
     * 是否设置为保留消息，请参考mqtt协议保留消息的设计含义
     * https://www.emqx.io/docs/zh/v4.4/advanced/retained.html
     */
    private Boolean retain = Boolean.FALSE;

    /**
     * 历史记录中是否显示该消息
     */
    private Boolean show = Boolean.TRUE;

    /**
     * 是否持久化
     */
    private Boolean persistence = Boolean.TRUE;
}
