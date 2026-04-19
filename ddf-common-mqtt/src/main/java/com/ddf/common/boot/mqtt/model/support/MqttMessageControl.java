package com.ddf.common.boot.mqtt.model.support;

import com.ddf.common.boot.mqtt.enume.MqttQosEnum;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>mqtt 发送mqtt消息控制相关参数 </p >
 * <p>
 * 优化说明：
 * 1. 使用枚举单例模式替代静态字段，避免序列化问题
 * 2. 枚举是 JVM 保障的天然线程安全单例
 * </p>
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/19 11:45
 */
@Data
public class MqttMessageControl implements Serializable {

    @Serial
    private static final long serialVersionUID = -5649107898855362417L;

    /**
     * 默认配置单例，使用枚举模式避免序列化问题和线程安全问题
     */
    public enum DefaultHolder {
        INSTANCE;

        private final MqttMessageControl value;

        DefaultHolder() {
            MqttMessageControl control = new MqttMessageControl();
            control.setQos(MqttQosEnum.AT_LAST_ONCE);
            control.setRetain(Boolean.FALSE);
            control.setShow(Boolean.FALSE);
            control.setPersistence(Boolean.FALSE);
            control.setIncludeSender(Boolean.FALSE);
            control.setAsync(Boolean.FALSE);
            this.value = control;
        }

        public MqttMessageControl get() {
            return value;
        }
    }

    /**
     * 获取默认配置
     *
     * @return 默认消息控制参数
     */
    public static MqttMessageControl getDefault() {
        return DefaultHolder.INSTANCE.get();
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
    private Boolean show = Boolean.FALSE;

    /**
     * 是否持久化
     */
    private Boolean persistence = Boolean.FALSE;

    /**
     * 发送方是否接收该消息
     */
    private Boolean includeSender = Boolean.FALSE;

     /**
      * 是否异步发送
      */
    private Boolean async = Boolean.FALSE;
}
