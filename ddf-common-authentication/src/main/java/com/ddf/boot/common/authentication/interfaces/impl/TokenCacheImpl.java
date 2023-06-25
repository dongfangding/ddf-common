package com.ddf.boot.common.authentication.interfaces.impl;

import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.authentication.interfaces.RedisTemplateSupport;
import com.ddf.boot.common.core.authentication.AuthenticationProperties;
import com.ddf.boot.common.core.authentication.TokenCache;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2023/06/25 14:44
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class TokenCacheImpl implements TokenCache {

    private StringRedisTemplate stringRedisTemplate = RedisTemplateSupport.defaultRedisTemplate();
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
     * @param uid
     * @return
     */
    public String getTokenKey(String uid) {
        return String.format(TOKEN_KEY, environmentHelper.getApplicationName(), uid);
    }

    @PostConstruct
    public void init() {
        if (SpringContextHolder.containsBeanType(RedisTemplateSupport.class)) {
            stringRedisTemplate = SpringContextHolder.getBean(RedisTemplateSupport.class).getStringRedisTemplate();
        }
    }

    @Override
    public void setToken(UserClaim userClaim, AuthenticateToken authenticateToken) {
        // token存入缓存
        stringRedisTemplate.opsForValue().set(getTokenKey(userClaim.getUserId()), authenticateToken.getToken(),
                authenticationProperties.getExpiredMinute(), TimeUnit.MINUTES);
    }

    @Override
    public String getToken(String userId) {
        return stringRedisTemplate.opsForValue().get(getTokenKey(userId));
    }

    @Override
    public void refreshToken(String userId, String token) {
        // token存入缓存
        stringRedisTemplate.opsForValue().set(getTokenKey(userId), token,
                authenticationProperties.getExpiredMinute(), TimeUnit.MINUTES);
    }
}
