package com.ddf.boot.common.authentication.util;

import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.RequestContext;

;

/**
 * <p>获取当前用户信息</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/05 22:33
 */
public class UserContextUtil {

    private static final ThreadLocal<UserClaim> USER_CONTEXT = ThreadLocal.withInitial(UserClaim::new);
    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = ThreadLocal.withInitial(RequestContext::new);

    /**
     * 获取当前用户信息
     *
     * @return
     */
    public static UserClaim getUserClaim() {
        return USER_CONTEXT.get();
    }

    /**
     * 填充用户信息
     *
     * @param userClaim
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

    /**
     * 获取当前用户uid
     *
     * @return
     */
    public static String getUserId() {
        return getUserClaim().getUserId();
    }

    public static Long getLongUserId() {
        return Long.parseLong(getUserId());
    }

    /**
     * 获取客户端唯一标识, 建议使用设备号
     *
     * @return
     */
    public static String getCredit() {
        return getUserClaim().getCredit();
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
