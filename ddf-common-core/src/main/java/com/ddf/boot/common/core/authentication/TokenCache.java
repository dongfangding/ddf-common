package com.ddf.boot.common.core.authentication;


import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;

/**
 * <p>用来管理认证token缓存的接口</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/06/25 13:10
 */
public interface TokenCache {

    /**
     * 设置token到缓存中
     *
     * @param userClaim 用户声明信息
     * @param authenticateToken 认证令牌对象
     */
    void setToken(UserClaim userClaim, AuthenticateToken authenticateToken);


    /**
     * 从缓存中取出用户的token
     *
     * @param userId 用户 ID
     * @return
     */
    String getToken(String userId);

    /**
     * 刷新token
     *
     * @param userId 用户 ID
     * @param token token 字符串
     */
    void refreshToken(String userId, String token);
}
