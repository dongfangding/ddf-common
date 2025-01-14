package com.ddf.boot.common.api.model.common.request;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>请求头通用参数枚举</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2021/12/31 17:56
 */
public enum RequestHeaderEnum {

    /**
     * 加签字段
     */
    SIGN("sign"),


    /**
     * 版本号（内部）
     * 之后做版本控制使用
     * eg: 10
     */
    VERSION_CODE("version_code"),

    /**
     * 版本号名称（版本）
     * eg: 1.0.0
     */
    VERSION("version"),

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
     * APP设置的语言
     */
    APP_LANGUAGE("app_language"),

    /**
     * 系统当前语言
     */
    SYSTEM_LANGUAGE("system_language"),

    /**
     * 运营商
     */
    SIM_OPERATOR("sim_operator"),

    /**
     * 是否使用了代理
     */
    USE_PROXY("use_proxy"),

    /**
     * 是否使用VPN
     */
    USE_VPN("use_vpn"),


    /**
     * 时区
     */
    TIME_ZONE("time_zone"),

    /**
     * 移动设备系统版本
     */
    OS_VERSION("os_version"),

    /**
     * 设备型号
     */
    DEVICE_MODE("device_mode"),


    /**
     * h5版本号
     */
    H5_VERSION("h5_version"),


    /**
     * ios的idfa
     */
    IOS_IDFA("ios_idfa"),


    /**
     * Android oa_id
     */
    OA_ID("oa_id"),

    /**
     * Android android_id
     */
    ANDROID_ID("android_id"),

    /**
     * 客户端ip
     */
    CLIENT_IP("client_ip"),


    /**
     * 客户端ip(比如有网关服务的前提下，网关负责解析就需要传递下去，而不是下游服务自己获取）
     */
    CLIENT_IP_FROM_GATEWAY("client_ip_from_gateway"),

    /**
     * 用户id(比如有网关服务的前提下，网关负责解析就需要传递下去，而不是下游服务自己获取）
     */
    USER_ID_FROM_GATEWAY("user_id_from_gateway"),

    /**
     * 链路追踪id(比如有网关服务的前提下，网关负责解析就需要传递下去，而不是下游服务自己获取）
     */
    TRACE_ID_FROM_GATEWAY("trace_id_from_gateway"),

    /**
     * 广告渠道（下载渠道）
     */
    AD_CHANNEL("ad_channel"),

    /**
     * 是否网关调用
     */
    IS_GATEWAY_DISPATCH("is_gateway_dispatch")


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
