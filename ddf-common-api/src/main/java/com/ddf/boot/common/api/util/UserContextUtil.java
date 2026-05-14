package com.ddf.boot.common.api.util;

import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.dto.RequestContext;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

;

/**
 * <p>获取当前用户信息</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2021/02/05 22:33
 */
public class UserContextUtil {

    private static final ThreadLocal<UserClaim> USER_CONTEXT = ThreadLocal.withInitial(UserClaim::new);
    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = ThreadLocal.withInitial(RequestContext::new);

    /**
     * 获取当前用户信息
     */
    public static UserClaim getUserClaim() {
        return USER_CONTEXT.get();
    }

    /**
     * 填充用户信息
     *
     * @param userClaim 用户声明信息
     */
    public static void setUserClaim(UserClaim userClaim) {
        USER_CONTEXT.set(userClaim);
    }

    /**
     * 移除用户信息
     */
    public static void removeUserClaim() {
        USER_CONTEXT.remove();
    }

    public static String getClientIp() {
        return getRequestContext().getClientIp();
    }

    public static String getLanguage() {
        return getRequestContext().getLanguage();
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

    public static Integer getVersionCode() {
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

    public static String getOsVersion() {
        return getRequestContext().getOsVersion();
    }


    public static String getUserId() {
        return getRequestContext().getUserIdFromGateway();
    }

    public static String getDeviceMode() {
        return getRequestContext().getDeviceMode();
    }

    public static Long getLongUserId() {
        return Long.parseLong(getUserId());
    }

    /**
     * 设置请求上下文
     *
     * @param requestHeader 请求上下文对象
     */
    public static void setRequestContext(RequestContext requestHeader) {
        REQUEST_CONTEXT.set(requestHeader);
    }

    /**
     * 获取请求上下文
     */
    public static RequestContext getRequestContext() {
        return REQUEST_CONTEXT.get();
    }

    /**
     * 移除请求上下文
     */
    public static void removeRequestContext() {
        REQUEST_CONTEXT.remove();
    }


    /**
     * 获取当前app语言，默认英文
     */
    public static String getLanguageOrDefault() {
        String defaultLanguage = "en";
        return StringUtils.defaultIfBlank(getRequestContext().getLanguage(), defaultLanguage);
    }

    public static Locale getLocale() {
        return new Locale(getLanguageOrDefault());
    }
}
