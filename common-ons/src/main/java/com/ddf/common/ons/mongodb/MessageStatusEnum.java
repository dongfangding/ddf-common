package com.ddf.common.ons.mongodb;

import lombok.Getter;

/**
 *
 * 消息状态
 *
 * @author snowball
 * @date 2021/8/26 15:32
 **/
public enum MessageStatusEnum {

    SUCCESS(1, "成功", "Success"),
    FAILURE(0, "失败", "Failure");

    @Getter
    private Integer value;
    private String desc;
    private String collectionNameSuffix;

    MessageStatusEnum(Integer value, String desc, String collectionNameSuffix) {
        this.value = value;
        this.desc = desc;
        this.collectionNameSuffix = collectionNameSuffix;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public String getCollectionNameSuffix() {
        return collectionNameSuffix;
    }
}
