package com.ddf.boot.common.authentication.util;

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
}
