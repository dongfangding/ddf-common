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
     * 认证请求头字段
     */
    AUTH_HEADER("Authorization"),

    /**
     * 加签字段
     */
    SIGN("sign"),

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
