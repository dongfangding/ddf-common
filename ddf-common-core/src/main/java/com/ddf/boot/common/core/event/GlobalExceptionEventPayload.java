package com.ddf.boot.common.core.event;

import java.util.Map;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2024/06/05 19:23
 */
@Data
public class GlobalExceptionEventPayload {

    /**
     * 接口名
     */
    private String url;

    /**
     * 查询参数
     */
    private Map<String, String[]> parameterMap;

    /**
     * 请求体
     */
    private String body;


    /**
     * 当前主机地址
     */
    private String host;

    /**
     * 当前应用名称
     */
    private String applicationName;

    /**
     * 当前应用profile
     */
    private String profile;

    /**
     * 异常时间
     */
    private Long timestamps;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 设备号
     */
    private String imei;

    /**
     * 从网关获取的用户唯一标识符
     */
    private String uid;

    /**
     * os
     */
    private String os;

    /**
     * 是否网关转发
     */
    private Boolean isGatewayDispatch = Boolean.FALSE;

    /**
     * 版本code
     */
    private Integer versionCode;

    /**
     * 客户端请求头
     */
    private Map<String, String> clientHeaderMap;


}
