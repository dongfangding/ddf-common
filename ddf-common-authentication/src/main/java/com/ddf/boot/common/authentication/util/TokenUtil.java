package com.ddf.boot.common.authentication.util;

import com.ddf.boot.common.authentication.model.AuthenticateCheckResult;
import com.ddf.boot.common.authentication.model.AuthenticateToken;
import com.ddf.boot.common.authentication.model.UserClaim;
import com.ddf.boot.common.core.exception200.AccessDeniedException;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.SecureUtil;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

;

/**
 * <p>token生成工具</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/24 22:29
 */
@Slf4j
public class TokenUtil {

    private TokenUtil() {}

    /**
     * 生成token规则
     *
     * @param userClaim
     * @return
     */
    public static AuthenticateToken createToken(UserClaim userClaim) {
        final String originUserClaimStr = JsonUtil.asString(userClaim);
        return AuthenticateToken.of(SecureUtil.bCryptEncoder(userClaim.getUserId()), SecureUtil.encryptHexByAES(originUserClaimStr));
    }

    /**
     * 从完整token中解析用户信息
     *
     * @param token
     * @return
     */
    public static UserClaim getUserClaim(String token) {
        UserClaim claim;
        try {
            final AuthenticateToken tokenObj = AuthenticateToken.fromToken(token);
            final String originDetailsToken = SecureUtil.decryptFromHexByAES(tokenObj.getDetailsToken());
            claim = JsonUtil.toBean(originDetailsToken, UserClaim.class);
            if (Objects.isNull(claim)) {
                throw new AccessDeniedException("无法获取到用户信息");
            }
        } catch (Exception e) {
            log.error("token解析失败, ", e);
            throw new AccessDeniedException("token解析失败");
        }
        return claim;
    }

    /**
     * 解析token并验证token本身规则
     *
     * @param token
     * @return
     */
    public static AuthenticateCheckResult checkToken(String token) {
        final AuthenticateToken authenticateToken = AuthenticateToken.fromToken(token);
        final String originDetailsToken = SecureUtil.decryptFromHexByAES(authenticateToken.getDetailsToken());
        UserClaim userClaim = JsonUtil.toBean(originDetailsToken, UserClaim.class);
        String userId = userClaim.getUserId();
        final boolean bool = SecureUtil.bCryptMatch(userId, authenticateToken.getUserIdToken());
        if (!bool) {
            throw new AccessDeniedException("被伪造的身份信息签名");
        }
        return AuthenticateCheckResult.of(authenticateToken, userClaim);
    }
}
