package com.ddf.common.ons.console.constant;

import lombok.Getter;

/**
 * <p>指定创建的Group ID适用的协议</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/20 15:34
 */
@Getter
public enum GroupTypeEnum {

    /**
     * 协议
     */
    TCP("tcp"),
    HTTP("http")

    ;


    GroupTypeEnum(String value) {
        this.value = value;
    }

    private final String value;
}
