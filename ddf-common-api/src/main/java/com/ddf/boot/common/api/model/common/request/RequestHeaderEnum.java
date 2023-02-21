package com.ddf.boot.common.api.model.common.request;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 主要是移动端设备，需要识别到具体设备号的时候, 如果是pc设备，需要Pc自己按照对应规则生成一个设备号放到本地使用，再未清除之前一直使用这个生成的
     */
    IMEI("imei"),

    /**
     * 防重放字段， 毫秒时间戳
     */
    NONCE("nonce"),

    /**
     * 版本号
     */
    VERSION("version"),

    /**
     * 经度
     */
    LONGITUDE("longitude"),

    /**
     * 纬度
     */
    LATITUDE("latitude")

    ;
    private final String name;

    private static final Map<String, RequestHeaderEnum> MAPPINGS;

    static {
        MAPPINGS = Arrays.stream(values()).collect(Collectors.toMap(RequestHeaderEnum::getName, obj -> obj));
    }

    RequestHeaderEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
