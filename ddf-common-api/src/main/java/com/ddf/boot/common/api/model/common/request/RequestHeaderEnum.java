package com.ddf.boot.common.api.model.common.request;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * <p>请求头通用参数枚举</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2021/12/31 17:56
 */
public enum RequestHeaderEnum {

    /**
     * 加签字段
     */
    SIGN("sign", true, true, ""),

    /**
     * 版本号（内部）
     * 之后做版本控制使用
     * eg: 10
     */
    VERSION_CODE("version_code", true, true, "100"),

    /**
     * 版本号名称（版本）
     * eg: 1.0.0
     */
    VERSION("version", true, true, "1.0.0"),

    /**
     * 客户端操作系统， 如 pc/ios/android
     */
    OS("os", true, true, ""),

    /**
     * 客户端设备唯一标识，
     * 主要是移动端设备，需要识别到具体设备号的时候, 如果是pc设备，需要Pc自己按照对应规则生成一个设备号放到本地使用，再未清除之前一直使用这个生成的
     */
    IMEI("imei", true, true, ""),

    /**
     * 防重放字段， 毫秒时间戳
     */
    NONCE("nonce", true, true, ""),

    /**
     * APP设置的语言
     */
    LANGUAGE("language", false, true, ""),

    /**
     * 时区
     */
    TIME_ZONE("time_zone", false, true, "false"),

    /**
     * 移动设备系统版本
     */
    OS_VERSION("os_version", false, true, ""),

    /**
     * 设备型号
     */
    DEVICE_MODE("device_mode", false, true, ""),

    /**
     * 客户端ip
     */
    CLIENT_IP("client_ip", false, false, ""),

    /**
     * 经度
     */
    LONGITUDE("longitude", false, true, "0"),

    /**
     * 纬度
     */
    LATITUDE("latitude", false, true, "0"),

    /**
     * 客户端ip(比如有网关服务的前提下，网关负责解析就需要传递下去，而不是下游服务自己获取）
     */
    CLIENT_IP_FROM_GATEWAY("client_ip_from_gateway", false, false, ""),

    /**
     * 用户id(比如有网关服务的前提下，网关负责解析就需要传递下去，而不是下游服务自己获取）
     */
    USER_ID_FROM_GATEWAY("user_id_from_gateway", false, false, ""),

    /**
     * 链路追踪id(比如有网关服务的前提下，网关负责解析就需要传递下去，而不是下游服务自己获取）
     */
    TRACE_ID_FROM_GATEWAY("trace_id_from_gateway", false, false, ""),

    /**
     * 是否网关调用
     */
    IS_GATEWAY_DISPATCH("is_gateway_dispatch", false, false, "true"),
    ;
    /**
     * header name
     */
    @Getter
    private final String name;
    /**
     * 是否必须传递
     */
    @Getter
    private final boolean isRequired;
    /**
     * 是否客户端请求头， 客户端请求头会被统一处理， 非客户端请求头需要自己处理
     */
    @Getter
    private final boolean isClientHeader;
    /**
     * 默认值
     */
    @Getter
    private final String defaultValue;

    private static final Map<String, RequestHeaderEnum> MAPPINGS;

    /**
     * 客户端传递的请求头
     */
    private static final Map<String, RequestHeaderEnum> ALL_CLIENT_HEADERS;
    /**
     * 所有客户端必须传递的请求头
     */
    private static final Map<String, RequestHeaderEnum> REQUIRED_CLIENT_HEADERS;

    static {
        MAPPINGS = Arrays
                .stream(values())
                .collect(Collectors.toMap(RequestHeaderEnum::getName, obj -> obj));
        ALL_CLIENT_HEADERS = Arrays
                .stream(values())
                .filter(RequestHeaderEnum::isClientHeader)
                .collect(Collectors.toMap(RequestHeaderEnum::getName, obj -> obj));
        REQUIRED_CLIENT_HEADERS = Arrays
                .stream(values())
                .filter(obj -> obj.isClientHeader() && obj.isRequired())
                .collect(Collectors.toMap(RequestHeaderEnum::getName, obj -> obj));
    }
    RequestHeaderEnum(String name, boolean isRequired, boolean isClientHeader, String defaultValue) {
        this.name = name;
        this.isRequired = isRequired;
        this.isClientHeader = isClientHeader;
        this.defaultValue = defaultValue;
    }

    public static Map<String, RequestHeaderEnum> getAllMappings() {
        return MAPPINGS;
    }

    public static Map<String, RequestHeaderEnum> getAllClientHeaders() {
        return ALL_CLIENT_HEADERS;
    }

    public static Map<String, RequestHeaderEnum> getRequiredClientHeaders() {
        return REQUIRED_CLIENT_HEADERS;
    }

    /**
     * 解析客户端请求头，将请求中的值映射为字符串。
     * 如果请求头不存在，则使用默认值。
     *
     * @param request HTTP 请求对象
     * @return 请求头名称到值的映射
     */
    public static Map<String, String> resolveClientHeaders(HttpServletRequest request) {
        Map<String, String> clientHeaderMap = new HashMap<>(32);
        ALL_CLIENT_HEADERS.forEach((name, headerEnum) -> {
            clientHeaderMap.put(name, Optional.ofNullable(request.getHeader(name)).orElse(headerEnum.getDefaultValue()));
        });
        return clientHeaderMap;
    }
}
