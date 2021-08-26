package com.ddf.common.ons.console.model;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * <p>ONS错误码</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/24 15:27
 */
@Getter
public enum OnsBizErrorCodeEnum implements BaseCallbackCode {

    /**
     * ONS错误码
     * https://help.aliyun.com/document_detail/44425.html?spm=a2c4g.11186623.6.681.7a4051afN9iUQR
     *
     */
    ONS_SYSTEM_ERROR("ONS_SYSTEM_ERROR", "消息队列 RocketMQ 版后端异常"),
    ONS_SERVICE_UNSUPPORTED("ONS_SERVICE_UNSUPPORTED", "当前调用在对应的地域不支持"),
    ONS_INVOKE_ERROR("ONS_INVOKE_ERROR", "OpenAPI 接口调用失败"),
    BIZ_FIELD_CHECK_INVALID("BIZ_FIELD_CHECK_INVALID", "参数检验失败"),
    BIZ_TOPIC_NOT_FOUND("BIZ_TOPIC_NOT_FOUND", "Topic 没有找到"),
    BIZ_SUBSCRIPTION_NOT_FOUND("BIZ_SUBSCRIPTION_NOT_FOUND", "目标订阅关系 GID 找不到"),
    BIZ_PUBLISHER_EXISTED("BIZ_PUBLISHER_EXISTED", "指定 GID 已经存在"),
    BIZ_SUBSCRIPTION_EXISTED("BIZ_SUBSCRIPTION_EXISTED", "指定 GID 已经存在"),
    BIZ_CONSUMER_NOT_ONLINE("BIZ_CONSUMER_NOT_ONLINE", "指定 GID 的客户端不在线"),
    BIZ_NO_MESSAGE("BIZ_NO_MESSAGE", "当前查询条件没有匹配消息"),
    BIZ_REGION_NOT_FOUND("BIZ_REGION_NOT_FOUND", "请求的 Region 找不到"),
    BIZ_TOPIC_EXISTED("BIZ_TOPIC_EXISTED", "指定 Topic 已经存在"),
    BIZ_PUBLISH_INFO_NOT_FOUND("BIZ_PUBLISH_INFO_NOT_FOUND", "请求的 GID 没有找到"),
    EMPOWER_EXIST_ERROR("EMPOWER_EXIST_ERROR", "当前授权关系已经存在"),
    EMPOWER_OWNER_CHECK_ERROR("EMPOWER_OWNER_CHECK_ERROR", "当前用户不是授权 Topic 的 Owner"),
    AUTH_RESOURCE_OWNER_ERROR("AUTH_RESOURCE_OWNER_ERROR", "没有权限操作或者资源不存在")

    ;

    private final String code;

    private final String description;

    private final String bizDesc;

    private static final Map<String, OnsBizErrorCodeEnum> VALUE_MAPPINGS;

    static {
        VALUE_MAPPINGS = Arrays.stream(values()).collect(Collectors.toMap(OnsBizErrorCodeEnum::getCode, val -> val));
    }


    OnsBizErrorCodeEnum(String value, String description) {
        this.code = value;
        this.description = description;
        this.bizDesc = description;
    }

    /**
     * 根据value获取对象
     *
     * @param value
     * @return
     */
    public static OnsBizErrorCodeEnum getByValue(String value) {
        return VALUE_MAPPINGS.get(value);
    }
}
