package com.ddf.boot.common.api.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>客户端类型</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/12/30 14:02
 */
public enum OsEnum {

    /**
     * 未知
     */
    UNKNOWN,


    /**
     * 安卓
     */
    ANDROID,

    /**
     * 苹果
     */
    IOS,

    /**
     * pc
     */
    PC,

    H5

    ;

    /**
     * 解析枚举值
     *
     * @param value 参数值
     * @return
     */
    public static OsEnum resolve(String value) {
        if (StringUtils.isBlank(value)) {
            return OsEnum.UNKNOWN;
        }
        try {
            return valueOf(value);
        } catch (Exception e) {
            return OsEnum.UNKNOWN;
        }
    }
}
