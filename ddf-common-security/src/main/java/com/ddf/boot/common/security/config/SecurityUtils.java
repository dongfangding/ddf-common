package com.ddf.boot.common.security.config;

import com.ddf.boot.common.jwt.model.UserClaim;
import com.ddf.boot.common.exception.GlobalCustomizeException;
import com.ddf.boot.common.exception.GlobalExceptionEnum;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录的用户
 *
 * @author dongfang.ding
 * @date 2019/9/17 10:02
 */
public class SecurityUtils {

    public static <T> UserClaim<T> getUserDetails(boolean throwException) {
        UserClaim<T> userClaim;
        try {
            userClaim = (UserClaim) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            if (throwException) {
                throw new GlobalCustomizeException(GlobalExceptionEnum.LOGIN_EXPIRED);
            }
            return UserClaim.defaultUser();
        }
        return userClaim;
    }

    /**
     * 获取当前用户名称
     *
     * @return 系统用户名称
     */
    public static <T> String getUsername() {
        UserClaim<T> userClaim = getUserDetails(true);
        return userClaim.getUsername();
    }

    /**
     * 获取当前用户id
     *
     * @return 系统用户id
     */
    public static <T> String getDefaultUserName() {
        UserClaim<T> userClaim = getUserDetails(false);
        return userClaim.getUsername();
    }



    /**
     * 获取当前用户id
     *
     * @return 系统用户id
     */
    public static <T> String getUserId() {
        UserClaim<T> userClaim = getUserDetails(true);
        return userClaim.getUserId();
    }

    /**
     * 获取当前用户id
     *
     * @return 系统用户id
     */
    public static <T> String getDefaultUserId() {
        UserClaim<T> userClaim = getUserDetails(false);
        return userClaim.getUserId();
    }
}
