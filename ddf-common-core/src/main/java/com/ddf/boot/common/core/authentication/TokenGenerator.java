package com.ddf.boot.common.core.authentication;

import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;

/**
 * token 生成/校验策略接口，接入方可注册自定义实现替换默认的 AES 实现。
 */
public interface TokenGenerator {

    AuthenticateToken createToken(UserClaim userClaim);

    UserClaim getUserClaim(String token);

    AuthenticateCheckResult checkToken(String token);

    void refreshToken(String userId, String token);
}
