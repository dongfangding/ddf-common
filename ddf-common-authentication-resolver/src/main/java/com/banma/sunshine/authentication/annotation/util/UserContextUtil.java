package com.banma.sunshine.authentication.annotation.util;

import com.boot.common.api.enums.OsEnum;
import com.boot.common.api.model.common.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

;

/**
 * <p>获取当前用户信息</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/05 22:33
 */
@Slf4j
public class UserContextUtil {

    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = ThreadLocal.withInitial(RequestContext::new);

    public static String getClientIp() {
        return getRequestContext().getClientIp();
    }

    public static String getClientIpFromGateway() {
        return getRequestContext().getClientIpFromGateway();
    }

    public static String getRequestUri() {
        return getRequestContext().getRequestUri();
    }

    public static String getSign() {
        return getRequestContext().getSign();
    }

    public static String getVersion() {
        return getRequestContext().getVersion();
    }

    public static long getVersionCode(){
        return getRequestContext().getVersionCode();
    }

    public static String getImei() {
        return getRequestContext().getImei();
    }

    public static Long getNonce() {
        return getRequestContext().getNonce();
    }

    public static OsEnum getOs() {
        return getRequestContext().getOs();
    }

    public static BigDecimal getLongitude() {
        return getRequestContext().getLongitude();
    }

    public static BigDecimal getLatitude() {
        return getRequestContext().getLatitude();
    }

    public static String getUserId() {
        return getRequestContext().getUserIdFromGateway();
    }

    public static String getChannel() {
        return getRequestContext().getChannel();
    }

    public static Long getLongUserId() {
        try {
            return Long.parseLong(getUserId());
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return 0L;
        }
    }


    /**
     * 设置请求上下文
     *
     * @param requestHeader
     */
    public static void setRequestContext(RequestContext requestHeader) {
        REQUEST_CONTEXT.set(requestHeader);
    }

    /**
     * 获取请求上下文
     *
     * @return
     */
    public static RequestContext getRequestContext() {
        return REQUEST_CONTEXT.get();
    }

    /**
     * 移除请求上下文
     */
    public static void removeRequestContext() {
        REQUEST_CONTEXT.remove();;
    }
}
