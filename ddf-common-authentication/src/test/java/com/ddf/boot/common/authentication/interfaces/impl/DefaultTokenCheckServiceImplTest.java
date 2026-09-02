package com.ddf.boot.common.authentication.interfaces.impl;

import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DefaultTokenCheckServiceImpl 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class DefaultTokenCheckServiceImplTest {

    @Test
    @DisplayName("当请求 credit 与 token credit 不一致时应拒绝")
    void shouldRejectWhenCreditDoesNotMatch() {
        AuthenticationProperties properties = new AuthenticationProperties();
        UserClaimService userClaimService = mock(UserClaimService.class);
        DefaultTokenCheckServiceImpl service = new DefaultTokenCheckServiceImpl(properties, userClaimService);
        HttpServletRequest request = mock(HttpServletRequest.class);

        UserClaim tokenUserClaim = new UserClaim();
        tokenUserClaim.setUsername("tester");
        tokenUserClaim.setCredit("credit-a");

        AuthenticateCheckResult result = new AuthenticateCheckResult();
        result.setUserClaim(tokenUserClaim);

        when(request.getHeader(properties.getCreditHeaderName())).thenReturn("credit-b");
        when(request.getHeader("User-Agent")).thenReturn("agent");

        assertThrows(UnauthorizedException.class, () -> service.customizeCheck(request, result));
    }

    @Test
    @DisplayName("当 credit 一致时应返回最新用户信息")
    void shouldReturnStoreUserWhenCreditMatches() {
        AuthenticationProperties properties = new AuthenticationProperties();
        UserClaimService userClaimService = mock(UserClaimService.class);
        DefaultTokenCheckServiceImpl service = new DefaultTokenCheckServiceImpl(properties, userClaimService);
        HttpServletRequest request = mock(HttpServletRequest.class);

        UserClaim tokenUserClaim = new UserClaim();
        tokenUserClaim.setUsername("tester");
        tokenUserClaim.setCredit("credit-a");

        UserClaim storeUser = new UserClaim();
        storeUser.setUserId("1001");
        storeUser.setUsername("latest");
        storeUser.setCredit("credit-a");

        AuthenticateCheckResult result = new AuthenticateCheckResult();
        result.setUserClaim(tokenUserClaim);

        when(request.getHeader(properties.getCreditHeaderName())).thenReturn("credit-a");
        when(userClaimService.getStoreUserInfo(request, tokenUserClaim)).thenReturn(storeUser);

        UserClaim actual = service.customizeCheck(request, result);

        assertEquals("1001", actual.getUserId());
        assertEquals("latest", actual.getUsername());
    }
}
