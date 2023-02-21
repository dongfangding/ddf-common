package com.ddf.common.boot.mqtt.enume;

import lombok.Getter;

/**
 * <p>mqtt消息质量</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 10:57
 */
public enum MqttQosEnum {

    /**
     * qos = 0， 最多一次，最不可靠，消息只发送一次，不管是否发送成功或者到达或者确认
     * qos = 1, 最少一次， 如果发送失败， broker 会一直重传发送数据，直到 接收端收到消息， 可能会造成一条数据收到多次
     * qos = 2, 确切一次， 质量最高，能够保证接收到一定能收到数据，而且只收到一次。
     *
     * 一般而言， 能够用轮训代替的通知业务， qos=0就够了，丢一些数据也无所谓
     * 如果是一些数据变更类的通知， 如果只是通知，然后客户端不依赖于推送的数据做业务，那么用qos = 1， 也足够了。除非客户端要用推送过去的
     * 数据，那么就只能用qos = 2了，否则可能会存在最新数据被以前消息覆盖的问题（当然一般而言，这种时序性的东西，都要依赖于客户端根据时间
     * 判断是否是垃圾数据，消息本身要携带发送时间，这个时间要大于客户端最后一次处理的消息的时间）
     */
    AT_MOST_ONCE(0),
    AT_LAST_ONCE(1),
    EXACTLY_ONCE(2)
    ;
    @Getter
    private final Integer qos;

    MqttQosEnum(Integer qos) {
        this.qos = qos;
    }

}
