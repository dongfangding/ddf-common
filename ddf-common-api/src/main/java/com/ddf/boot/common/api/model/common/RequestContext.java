package com.ddf.boot.common.api.model.common;

import com.ddf.boot.common.api.enums.OsEnum;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>存储请求相关的参数的上下文对象</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2022/01/14 17:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext implements Serializable {

    private static final long serialVersionUID = -7528108356364083934L;

    /**
     * 客户端ip，当前服务从请求对象中自己获取的
     */
    private String clientIp;

    /**
     * 通过网关传过来的客户端ip, 两个ip在不同的场景下自己决定用哪个值才是对的
     */
    private String clientIpFromGateway;

    /**
     * 请求路径
     */
    private String requestUri;

    /**
     * 签名字段
     */
    private String sign;

    /**
     * 内部版本号
     */
    private Long versionCode;

    /**
     * 版本名称
     */
    private String version;

    /**
     * 设备号
     */
    private String imei;

    /**
     * 防重放，时间毫秒值
     */
    private Long nonce;

    /**
     * 软件当前语言
     */
    private String appLanguage;

    /**
     * 设备系统当前语言
     */
    private String systemLanguage;

    /**
     * 客户端类型
     */
    private OsEnum os;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 应用渠道， 如苹果商店， 应用宝之类的
     */
    private String channel;

    /**
     * 设备运营商
     */
    private String simOperator;

    /**
     * 是否使用了代理
     */
    private Boolean useProxy;

    /**
     * 是否使用了vpn
     */
    private Boolean useVpn;

    /**
     * 设备时区
     */
    private String timeZone;

    /**
     * 从请求头中获取到的用户id, 存在网关时使用
     */
    private String userIdFromGateway;

    /**
     * 设备型号
     */
    private String deviceMode;

    /**
     * h5版本
     */
    private String h5Version;

    /**
     *  ios的idfa
     */
    private String iosIdfa;

    /**
     * Android oa_id
     */
    private String oaId;

    /**
     * Android android_id
     */
    private String androidId;

    /**
     * 是否网关转发
     */
    private Boolean isGatewayDispatch = Boolean.FALSE;

    /**
     * 自定义属性
     */
    private Map<String, Object> properties = new HashMap<>();

}
