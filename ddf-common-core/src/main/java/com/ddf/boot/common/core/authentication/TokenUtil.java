package com.ddf.boot.common.core.authentication;


import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.api.exception.BusinessException;
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
import com.google.common.base.Throwables;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

;

/**
 * <p>token生成工具</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2022/05/24 22:29
 */
@Slf4j
public class TokenUtil {

    private static final EnvironmentHelper ENVIRONMENT_HELPER = SpringContextHolder.getBeanWithStatic(EnvironmentHelper.class);
    private static final TokenCache TOKEN_CACHE = SpringContextHolder.getBeanWithStatic(TokenCache.class);

    private TokenUtil() {}


    /**
     * 生成token规则
     *
     * @param userClaim
     * @return
     */
    public static AuthenticateToken createToken(UserClaim userClaim) {
        final String originUserClaimStr = JsonUtil.asString(userClaim);
        final AuthenticateToken authenticateToken = AuthenticateToken.of(
                SecureUtil.bCryptEncoder(userClaim.getUserId()), SecureUtil.aesEncryptHex(originUserClaimStr));
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
            final String originDetailsToken = SecureUtil.aesDecryptStr(tokenObj.getDetailsToken());
            claim = JsonUtil.toBean(originDetailsToken, UserClaim.class);
            if (Objects.isNull(claim)) {
                throw new UnauthorizedException(CoreExceptionCode.ILLEGAL_TOKEN);
            }
        } catch (Exception e) {
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
        try {
            final AuthenticateToken authenticateToken = AuthenticateToken.fromToken(token);
            final String originDetailsToken = SecureUtil.aesDecryptStr(authenticateToken.getDetailsToken());
            UserClaim userClaim = JsonUtil.toBean(originDetailsToken, UserClaim.class);
            String userId = userClaim.getUserId();
            if (Objects.nonNull(TOKEN_CACHE)) {
                final String cacheToken = TOKEN_CACHE.getToken(userId);
                PreconditionUtil.checkArgument(StrUtil.isNotBlank(cacheToken), new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
                PreconditionUtil.checkArgument(Objects.equals(cacheToken, token), new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
            }
            return AuthenticateCheckResult.of(authenticateToken, userClaim);
        } catch (Exception e) {
            if(e instanceof UnauthorizedException){
                throw e;
            }
            log.error("[{}].checkToken().called with exception => token:{},e:{}","解析token失败",token, Throwables.getStackTraceAsString(e));
            throw new BusinessException(CoreExceptionCode.ILLEGAL_TOKEN);
        }
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

    public static void main(String[] args) {
        final AuthenticateCheckResult result = TokenUtil.checkToken(
                "$2a$10$GsXo2QZoLmQaRAR9S7S.MOcXCbvTiErPRMAX1unXM7sbMIbME.TpW<=>ad8c782851e20c70f1d25dfab92d0ee4f7de2424ba092721b9a38152ce1cc8ebe6c2b4e4c7d4c295f28ac741d0f63d74f6194fdabdb44e2cd9da1d2fcd8a517f1a827a2a9f3e77e9786118c2bee942e85d419d76768afeac3103d7bd92f27fbf");
        System.out.println("result = " + result);
    }
}
