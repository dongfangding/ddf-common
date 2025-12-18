package com.ddf.boot.common.api.model.common.dto;

import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.api.util.ReflectUtils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
    private Integer versionCode;

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
     * ios的idfa
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
     * 是否运营后台添加的设备白名单
     */
    private Boolean isConsoleWhitelistImei = Boolean.FALSE;

    /**
     * 账号是否被封（风控服务提供），被封之后，除开登录以外其他都会被拒绝
     */
    private Boolean isBanned = Boolean.FALSE;

    /**
     * 是否模拟器， 0否1是
     */
    private Boolean isSimulator = Boolean.FALSE;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 自定义属性
     */
    private Map<String, Object> properties = new HashMap<>();


    /**
     * 从请求头map中根据规则解析到RequestContext中
     *
     * @param headers
     * @return
     */
    public static RequestContext fromHeaderMap(Map<String, Object> headers) {
        RequestContext context = new RequestContext();
        final Map<String, Object> contextProperties = context.getProperties();
        headers.forEach((key, value) -> {
            RequestHeaderEnum headerEnum = RequestHeaderEnum
                    .getAllMappings()
                    .get(key);
            if (headerEnum != null) {
                Pattern pattern = Pattern.compile("_(.)");
                final String fieldName = getRequestContextFieldName(pattern, headerEnum);
                try {
                    ReflectUtils.setFiledValue(context, fieldName, value, true);
                } catch (Exception e) {
                    log.error("RequestContext设置属性失败，属性名：{}， value = {}", fieldName, value, e);
                    // 如果没有对应的字段就存入 properties
                    contextProperties.put(fieldName, value);
                }
            }
        });

        return context;
    }

    private static String getRequestContextFieldName(Pattern pattern, RequestHeaderEnum headerEnum) {
        Matcher matcher = pattern.matcher(headerEnum
                .name()
                .toLowerCase());
        StringBuilder fieldNameBuffer = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(fieldNameBuffer, matcher
                    .group(1)
                    .toUpperCase());
        }
        return fieldNameBuffer.toString();
    }
}
