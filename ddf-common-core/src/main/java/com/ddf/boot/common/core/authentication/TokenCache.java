package com.ddf.boot.common.core.authentication;

import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;

/**
 * <p>用来管理认证token缓存的接口</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2023/06/25 13:10
 */
public interface TokenCache {

    /**
     * 设置token到缓存中
     *
     * @param userClaim
     * @param authenticateToken
     */
    void setToken(UserClaim userClaim, AuthenticateToken authenticateToken);


    /**
     * 从缓存中取出用户的token
     *
     * @param userId
     * @return
     */
    String getToken(String userId);

    /**
     * 刷新token
     *
     * @param userId
     * @param token
     */
    void refreshToken(String userId, String token);
}
