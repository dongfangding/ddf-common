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
//        final AuthenticateToken authenticateToken = AuthenticateToken.of(
//                SecureUtil.bCryptEncoder(userClaim.getUserId()), SecureUtil.aesEncryptHex(originUserClaimStr));
        final AuthenticateToken authenticateToken = AuthenticateToken.of(
                SecureUtil.aesEncryptHex(userClaim.getUserId()), SecureUtil.aesEncryptHex(originUserClaimStr));
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
    /**
     * @param args 参数
     */
    public static void main(String[] args) {
        final AuthenticateCheckResult result = TokenUtil.checkToken(
                "$2a$10$dloe/PgLOVpgn2RqvfrgFucUOf8v9/HefZv0dLt/p04iGnD79N93G.6d3309a6d94d9a7bed598cbc372c75a9055507ce2bf965dfb83265925297c54e5fccd6c06ec4b3d629f77b6b5ffcba2270b3acb196f758d50f409a27684ba028b3b1d46a166564e9d816189bb92925baa1ca70b1ba3154dc5da2f96dd2fdd63a7af7d3f29b3cce4d9e26c1419c3a084397572758e3f90cfc1810993dfc2c8c3e84256b8120817f9c7674ffbfbb351f6fed84e8c3e076bd771b039f3ca6adbb2df71e28171937d668fd458311abf9caef39921b3f6825f0d681a35245eca337b49bc482a91ea761b84b7379d9a5647e805824ff9154499c9254344f3247000ed2");
        System.out.println("result = " + result);
    }
}