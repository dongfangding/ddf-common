package com.ddf.boot.common.core.authentication;

import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.core.helper.SpringContextHolder;

/**
 * @deprecated 请注入 {@link TokenGenerator} 使用；本类保留用于向后兼容。
 */
@Deprecated
public class TokenUtil {

    private static final TokenGenerator TOKEN_GENERATOR =
            SpringContextHolder.getBeanWithStatic(TokenGenerator.class);

    private TokenUtil() {
    }

    @Deprecated
    public static AuthenticateToken createToken(UserClaim userClaim) {
        return TOKEN_GENERATOR.createToken(userClaim);
    }

    @Deprecated
    public static UserClaim getUserClaim(String token) {
        return TOKEN_GENERATOR.getUserClaim(token);
    }

    @Deprecated
    public static AuthenticateCheckResult checkToken(String token) {
        return TOKEN_GENERATOR.checkToken(token);
    }

    @Deprecated
    public static void refreshToken(String userId, String token) {
        TOKEN_GENERATOR.refreshToken(userId, token);
    }
}
