package com.ddf.common.ons.console.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * <p>TOPIC消息类型</p >
 * <p>
 * 0：普通消息
 * 1：分区顺序消息
 * 2：全局顺序消息
 * 4：事务消息
 * 5：定时/延时消息
 * <p>
 * https://next.api.aliyun.com/document/Ons/2019-02-14/OnsTopicCreate
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 15:37
 */
@Getter
public enum TopicMessageType {

    /**
     * 创建的topic 消息类型
     */
    Normal_Msg(0, "普通消息"),
    Order_Msg(1, "分区顺序消息"),
    Trans_Msg_Half(2, "全局顺序消息"),
    Trans_msg_Commit(4, "事务消息"),
    Delay_Msg(5, "定时/延时消息");

    TopicMessageType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private final Integer value;

    private final String desc;

    private static final Map<Integer, TopicMessageType> VALUE_MAPPING;

    static {
        VALUE_MAPPING = Arrays.stream(values()).collect(Collectors.toMap(TopicMessageType::getValue, val -> val));
    }

    public static TopicMessageType getByValue(Integer value) {
        return VALUE_MAPPING.get(value);
    }

    public static String getDesc(Integer value) {
        final TopicMessageType messageType = getByValue(value);
        return Objects.nonNull(messageType) ? messageType.getDesc() : value + "";
    }
}
