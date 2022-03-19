package com.ddf.common.boot.mqtt.define;

import java.io.Serializable;

/**
 * <p>topic类型</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 15:41
 */
public interface MqttTopicType extends Serializable {

    /**
     * 获取topic完整前缀
     *
     * @return
     */
    String getFullTopicPrefix();
}
