package com.ddf.boot.common.core.authentication;


import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.constant.CoreExceptionCode;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.util.PreconditionUtil;
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

    private static final EnvironmentHelper ENVIRONMENT_HELPER = SpringContextHolder.getBeanWithStatic(EnvironmentHelper.class);
    private static final TokenCache TOKEN_CACHE = SpringContextHolder.getBeanWithStatic(TokenCache.class);

    /**
     * token key
     * %s application name 对应环境变量spring.application.name
     * %s uid
     */
    private static final String TOKEN_KEY = "%s:authentication:token:%s";


    private TokenUtil() {}

    /**
     * 获取token key规则
     *
     * @param uid
     * @return
     */
    public static String getTokenKey(String uid) {
        return String.format(TOKEN_KEY, Objects.isNull(ENVIRONMENT_HELPER) ? "" : ENVIRONMENT_HELPER.getApplicationName(), uid);
    }

    /**
     * 生成token规则
     *
     * @param userClaim
     * @return
     */
    public static AuthenticateToken createToken(UserClaim userClaim) {
        final String originUserClaimStr = JsonUtil.asString(userClaim);
        final AuthenticateToken authenticateToken = AuthenticateToken.of(
                SecureUtil.bCryptEncoder(userClaim.getUserId()), SecureUtil.encryptHexByAES(originUserClaimStr));
        if (Objects.nonNull(TOKEN_CACHE)) {
            TOKEN_CACHE.setToken(userClaim, authenticateToken);
        }
        return authenticateToken;
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
                throw new UnauthorizedException(CoreExceptionCode.ILLEGAL_TOKEN);
            }
        } catch (Exception e) {
            log.error("token解析失败, ", e);
            throw new UnauthorizedException(CoreExceptionCode.ILLEGAL_TOKEN);
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
        PreconditionUtil.checkArgument(bool, new UnauthorizedException(CoreExceptionCode.FORGE_TOKEN));
        if (Objects.nonNull(TOKEN_CACHE)) {
            final String cacheToken = TOKEN_CACHE.getToken(userId);
            PreconditionUtil.checkArgument(StrUtil.isNotBlank(cacheToken), new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
            PreconditionUtil.checkArgument(Objects.equals(cacheToken, token), new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
        }
        return AuthenticateCheckResult.of(authenticateToken, userClaim);
    }


    /**
     * 刷新用户token和过期时间
     *
     * @param userId
     * @param token
     */
    public static void refreshToken(String userId, String token) {
        if (Objects.nonNull(TOKEN_CACHE)) {
            TOKEN_CACHE.refreshToken(userId, token);
        }
    }
}
