package com.ddf.boot.common.authentication.interfaces.impl;

import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.interfaces.RedisTemplateSupport;
import com.ddf.boot.common.core.authentication.TokenCache;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2023/06/25 14:44
 */
@Slf4j
public class TokenCacheImpl implements TokenCache {

    public static final String BEAN_NAME = "tokenCacheImpl";

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthenticationProperties authenticationProperties;
    private final EnvironmentHelper environmentHelper;

    /**
     * token key
     * %s application name 对应环境变量spring.application.name
     * %s uid
     */
    private static final String TOKEN_KEY = "%s:authentication:token:%s";

    /**
     * 获取token key规则
     *
     * @param uid 用户 ID
     * @return
     */
    public String getTokenKey(String uid) {
        return TOKEN_KEY.formatted(environmentHelper.getApplicationName(), uid);
    }

    public TokenCacheImpl(AuthenticationProperties authenticationProperties, EnvironmentHelper environmentHelper,
            StringRedisTemplate defaultRedisTemplate, ObjectProvider<RedisTemplateSupport> redisTemplateSupport) {
        this.authenticationProperties = authenticationProperties;
        this.environmentHelper = environmentHelper;
        this.stringRedisTemplate = redisTemplateSupport
                .getIfAvailable(() -> () -> defaultRedisTemplate)
                .getStringRedisTemplate();
    }
    /**
     * @param userClaim 用户声明信息
     * @param authenticateToken 认证令牌对象
     */
    @Override
    public void setToken(UserClaim userClaim, AuthenticateToken authenticateToken) {
        // token存入缓存
        stringRedisTemplate.opsForValue().set(getTokenKey(userClaim.getUserId()), authenticateToken.getToken(),
                authenticationProperties.getExpiredMinute(), TimeUnit.MINUTES);
    }
    /**
     * @param userId 参数
     */
    @Override
    public String getToken(String userId) {
        return stringRedisTemplate.opsForValue().get(getTokenKey(userId));
    }
    /**
     * @param userId 参数
     * @param token token 字符串
     */
    @Override
    public void refreshToken(String userId, String token) {
        // token存入缓存
        stringRedisTemplate.opsForValue().set(getTokenKey(userId), token,
                authenticationProperties.getExpiredMinute(), TimeUnit.MINUTES);
    }
}
