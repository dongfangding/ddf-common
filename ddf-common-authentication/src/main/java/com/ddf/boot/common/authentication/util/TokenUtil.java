package com.ddf.boot.common.authentication.util;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.interfaces.RedisTemplateSupport;
import com.ddf.boot.common.authentication.model.AuthenticateCheckResult;
import com.ddf.boot.common.authentication.model.AuthenticateToken;
import com.ddf.boot.common.authentication.model.UserClaim;
import com.ddf.boot.common.core.exception200.UnauthorizedException;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.boot.common.core.util.SecureUtil;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

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

    private static StringRedisTemplate stringRedisTemplate = RedisTemplateSupport.defaultRedisTemplate();
    private static final AuthenticationProperties AUTHENTICATION_PROPERTIES = SpringContextHolder.getBean(AuthenticationProperties.class);
    private static final EnvironmentHelper ENVIRONMENT_HELPER = SpringContextHolder.getBean(EnvironmentHelper.class);

    /**
     * token key
     * %s application name 对应环境变量spring.application.name
     * %s uid
     */
    private static final String TOKEN_KEY = "%s:authentication:token:%s";

    static {
        if (SpringContextHolder.containsBeanType(RedisTemplateSupport.class)) {
            stringRedisTemplate = SpringContextHolder.getBean(RedisTemplateSupport.class).getStringRedisTemplate();
        }
    }

    private TokenUtil() {}

    /**
     * 获取token key规则
     *
     * @param uid
     * @return
     */
    public static String getTokenKey(String uid) {
        return String.format(TOKEN_KEY, ENVIRONMENT_HELPER.getApplicationName(), uid);
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
        // token存入缓存
        stringRedisTemplate.opsForValue().set(getTokenKey(userClaim.getUserId()), authenticateToken.getToken(),
                AUTHENTICATION_PROPERTIES.getExpiredMinute(), TimeUnit.MINUTES);
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
                throw new UnauthorizedException("无法获取到用户信息");
            }
        } catch (Exception e) {
            log.error("token解析失败, ", e);
            throw new UnauthorizedException("token解析失败");
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
        PreconditionUtil.checkArgument(bool, new UnauthorizedException("被伪造的身份信息签名"));
        final String cacheToken = stringRedisTemplate.opsForValue().get(getTokenKey(userId));
        PreconditionUtil.checkArgument(StrUtil.isNotBlank(cacheToken), new UnauthorizedException("登录信息已失效，请重新登录~"));
        PreconditionUtil.checkArgument(Objects.equals(cacheToken, token), new UnauthorizedException("已过期的凭据认证，请重新登录~"));
        return AuthenticateCheckResult.of(authenticateToken, userClaim);
    }
}
