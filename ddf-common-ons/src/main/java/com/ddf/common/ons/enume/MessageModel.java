package com.ddf.common.ons.enume;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息模式
 *
 * @author snowball
 * @since 2021/8/26 14:19
 **/
public enum MessageModel {

    /**
     * 广播模式
     */
    BROADCASTING("BROADCASTING", "广播模式"),

    /**
     * 集群模式
     */
    CLUSTERING("CLUSTERING", "集群模式");

    private final String model;

    private final String desc;

    private final static Map<String, MessageModel> VALUE_MAPPINGS;

    static {
        VALUE_MAPPINGS = Arrays.stream(values()).collect(Collectors.toMap(MessageModel::getModel, val -> val));
    }

    MessageModel(String model, String desc) {
        this.model = model;
        this.desc = desc;
    }

    public String getModel() {
        return this.model;
    }

    public String getDesc() {
        return this.desc;
    }

    /**
     * @param model 参数
     */
    public static MessageModel getByModel(String model) {
        return VALUE_MAPPINGS.get(model);
    }

    /**
     * @param model 参数
     */
    public static String getDescByModel(String model) {
        final MessageModel messageModel = getByModel(model);
        return Objects.nonNull(messageModel) ? messageModel.desc : model;
    }

    /**
     * @param model 参数
     */
    public boolean isMatch(String model) {
        return Objects.equals(getModel(), model);
    }
}
