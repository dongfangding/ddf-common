package com.ddf.boot.common.core.authentication;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.constant.CoreExceptionCode;
import com.ddf.boot.common.core.event.LoginSuccessEvent;
import com.ddf.boot.common.core.event.TokenRefreshEvent;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.boot.common.core.util.SecureUtil;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class DefaultTokenGenerator implements TokenGenerator {

    private final ObjectProvider<TokenCache> tokenCacheProvider;
    private final ObjectProvider<ApplicationEventPublisher> eventPublisherProvider;

    public DefaultTokenGenerator(ObjectProvider<TokenCache> tokenCacheProvider,
            ObjectProvider<ApplicationEventPublisher> eventPublisherProvider) {
        this.tokenCacheProvider = tokenCacheProvider;
        this.eventPublisherProvider = eventPublisherProvider;
    }

    @Override
    public AuthenticateToken createToken(UserClaim userClaim) {
        final String originUserClaimStr = JsonUtil.asString(userClaim);
        final AuthenticateToken authenticateToken = AuthenticateToken.of(
                SecureUtil.aesEncryptHex(userClaim.getUserId()), SecureUtil.aesEncryptHex(originUserClaimStr));
        final TokenCache tokenCache = tokenCacheProvider.getIfAvailable();
        if (Objects.nonNull(tokenCache)) {
            tokenCache.setToken(userClaim, authenticateToken);
        }
        ApplicationEventPublisher publisher = eventPublisherProvider.getIfAvailable();
        if (Objects.nonNull(publisher)) {
            publisher.publishEvent(new LoginSuccessEvent(this, userClaim));
        }
        return authenticateToken;
    }

    @Override
    public UserClaim getUserClaim(String token) {
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

    @Override
    public AuthenticateCheckResult checkToken(String token) {
        try {
            final AuthenticateToken authenticateToken = AuthenticateToken.fromToken(token);
            final String originDetailsToken = SecureUtil.aesDecryptStr(authenticateToken.getDetailsToken());
            UserClaim userClaim = JsonUtil.toBean(originDetailsToken, UserClaim.class);
            String userId = userClaim.getUserId();
            final TokenCache tokenCache = tokenCacheProvider.getIfAvailable();
            if (Objects.nonNull(tokenCache)) {
                final String cacheToken = tokenCache.getToken(userId);
                PreconditionUtil.checkArgument(StrUtil.isNotBlank(cacheToken),
                        new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
                PreconditionUtil.checkArgument(Objects.equals(cacheToken, token),
                        new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
            }
            return AuthenticateCheckResult.of(authenticateToken, userClaim);
        } catch (Exception e) {
            if (e instanceof UnauthorizedException) {
                throw e;
            }
            log.error("[{}].checkToken().called with exception => e:{}", "解析token失败", e.toString());
            throw new BusinessException(CoreExceptionCode.ILLEGAL_TOKEN);
        }
    }

    @Override
    public void refreshToken(String userId, String token) {
        final TokenCache tokenCache = tokenCacheProvider.getIfAvailable();
        if (Objects.nonNull(tokenCache)) {
            tokenCache.refreshToken(userId, token);
        }
        ApplicationEventPublisher publisher = eventPublisherProvider.getIfAvailable();
        if (Objects.nonNull(publisher)) {
            publisher.publishEvent(new TokenRefreshEvent(this, userId));
        }
    }
}
