package com.ddf.boot.common.core.model.request;

/**
 * <p>请求头通用参数枚举</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/12/31 17:56
 */
public enum RequestHeaderEnum {

    /**
     * 加签字段
     */
    SIGN("sign"),

    /**
     * 客户端操作系统， 如 pc/ios/android
     */
    OS("os"),

    /**
     * 客户端设备唯一标识，
     * 主要是移动端设备，需要识别到具体设备号的时候
     */
    IMEI("imei"),

    /**
     * 防重放字段， 毫秒时间戳
     */
    NONCE("nonce")

    ;
    String name;

    RequestHeaderEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
