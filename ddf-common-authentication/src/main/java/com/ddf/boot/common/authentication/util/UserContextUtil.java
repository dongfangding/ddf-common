package com.ddf.boot.common.authentication.util;

import com.ddf.boot.common.api.model.common.RequestHeader;
import com.ddf.boot.common.authentication.model.UserClaim;

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
    private static final ThreadLocal<RequestHeader> REQUEST_HEADER_CONTEXT = ThreadLocal.withInitial(RequestHeader::new);

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
     * 获取客户端唯一标识
     *
     * @return
     */
    public static String getCredit() {
        return getUserClaim().getCredit();
    }


    /**
     * 设置请求头
     *
     * @param requestHeader
     */
    public static void setRequestHeaderContext(RequestHeader requestHeader) {
        REQUEST_HEADER_CONTEXT.set(requestHeader);
    }

    /**
     * 获取请求头
     *
     * @return
     */
    public static RequestHeader getRequestHeader() {
        return REQUEST_HEADER_CONTEXT.get();
    }

    /**
     * 移除请求头上下文
     */
    public static void removeRequestHeader() {
        REQUEST_HEADER_CONTEXT.remove();;
    }
}
