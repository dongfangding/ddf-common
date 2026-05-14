package com.ddf.boot.common.authentication.interfaces.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.interfaces.RedisTemplateSupport;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * TokenCacheImpl 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class TokenCacheImplTest {

    @Test
    @DisplayName("应按应用名和用户ID生成 token key")
    void shouldBuildTokenKeyWithApplicationNameAndUserId() {
        AuthenticationProperties properties = new AuthenticationProperties();
        EnvironmentHelper environmentHelper = mock(EnvironmentHelper.class);
        StringRedisTemplate defaultRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ObjectProvider<RedisTemplateSupport> objectProvider = mock(ObjectProvider.class);
        StringRedisTemplate selectedRedisTemplate = mock(StringRedisTemplate.class);
        RedisTemplateSupport redisTemplateSupport = () -> selectedRedisTemplate;

        when(environmentHelper.getApplicationName()).thenReturn("demo-app");
        when(objectProvider.getIfAvailable(org.mockito.ArgumentMatchers.any())).thenReturn(redisTemplateSupport);

        TokenCacheImpl tokenCache = new TokenCacheImpl(properties, environmentHelper, defaultRedisTemplate,
                objectProvider);

        assertEquals("demo-app:authentication:token:1001", tokenCache.getTokenKey("1001"));
    }

    @Test
    @DisplayName("setToken、getToken、refreshToken 应委托给选中的 StringRedisTemplate")
    void shouldDelegateTokenOperationsToSelectedRedisTemplate() {
        AuthenticationProperties properties = new AuthenticationProperties();
        properties.setExpiredMinute(30);
        EnvironmentHelper environmentHelper = mock(EnvironmentHelper.class);
        StringRedisTemplate defaultRedisTemplate = mock(StringRedisTemplate.class);
        StringRedisTemplate selectedRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ObjectProvider<RedisTemplateSupport> objectProvider = mock(ObjectProvider.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        RedisTemplateSupport redisTemplateSupport = () -> selectedRedisTemplate;

        when(environmentHelper.getApplicationName()).thenReturn("demo-app");
        when(objectProvider.getIfAvailable(org.mockito.ArgumentMatchers.any())).thenReturn(redisTemplateSupport);
        when(selectedRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("demo-app:authentication:token:1001")).thenReturn("1001.detail-token");

        TokenCacheImpl tokenCache = new TokenCacheImpl(properties, environmentHelper, defaultRedisTemplate,
                objectProvider);
        UserClaim userClaim = UserClaim.mockUser("1001");
        AuthenticateToken authenticateToken = AuthenticateToken.of("1001", "detail-token");

        tokenCache.setToken(userClaim, authenticateToken);
        String token = tokenCache.getToken("1001");
        tokenCache.refreshToken("1001", "1001.new-detail");

        verify(valueOperations).set("demo-app:authentication:token:1001", "1001.detail-token", 30, TimeUnit.MINUTES);
        verify(valueOperations).get("demo-app:authentication:token:1001");
        verify(valueOperations).set("demo-app:authentication:token:1001", "1001.new-detail", 30, TimeUnit.MINUTES);
        assertEquals("1001.detail-token", token);
    }
}
