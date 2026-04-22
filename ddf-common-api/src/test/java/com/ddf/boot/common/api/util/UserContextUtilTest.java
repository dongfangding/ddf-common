package com.ddf.boot.common.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.dto.RequestContext;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UserContextUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class UserContextUtilTest {

    @AfterEach
    void tearDown() {
        UserContextUtil.removeUserClaim();
        UserContextUtil.removeRequestContext();
    }

    @Test
    @DisplayName("应返回默认上下文对象而不是 null")
    void shouldReturnDefaultContextObjects() {
        assertNotNull(UserContextUtil.getUserClaim());
        assertNotNull(UserContextUtil.getRequestContext());
        assertEquals("en", UserContextUtil.getLanguageOrDefault());
    }

    @Test
    @DisplayName("应读取已写入的用户与请求上下文")
    void shouldReadBackStoredUserAndRequestContext() {
        UserClaim claim = new UserClaim();
        claim.setUserId("1001");
        claim.setUsername("tester");

        RequestContext requestContext = new RequestContext();
        requestContext.setLanguage("zh");
        requestContext.setUserIdFromGateway("1001");
        requestContext.setClientIp("127.0.0.1");
        requestContext.setClientIpFromGateway("10.0.0.1");
        requestContext.setRequestUri("/demo");
        requestContext.setSign("signed");
        requestContext.setVersionCode(12);
        requestContext.setImei("imei-1");
        requestContext.setNonce(99L);
        requestContext.setOs(OsEnum.ANDROID);
        requestContext.setOsVersion("14");
        requestContext.setDeviceMode("Pixel");

        UserContextUtil.setUserClaim(claim);
        UserContextUtil.setRequestContext(requestContext);

        assertEquals("1001", UserContextUtil.getUserClaim().getUserId());
        assertEquals("tester", UserContextUtil.getUserClaim().getUsername());
        assertEquals("1001", UserContextUtil.getUserId());
        assertEquals(1001L, UserContextUtil.getLongUserId());
        assertEquals("127.0.0.1", UserContextUtil.getClientIp());
        assertEquals("10.0.0.1", UserContextUtil.getClientIpFromGateway());
        assertEquals("/demo", UserContextUtil.getRequestUri());
        assertEquals("signed", UserContextUtil.getSign());
        assertEquals(12, UserContextUtil.getVersionCode());
        assertEquals("imei-1", UserContextUtil.getImei());
        assertEquals(99L, UserContextUtil.getNonce());
        assertEquals(OsEnum.ANDROID, UserContextUtil.getOs());
        assertEquals("14", UserContextUtil.getOsVersion());
        assertEquals("Pixel", UserContextUtil.getDeviceMode());
        assertEquals("zh", UserContextUtil.getLanguage());
        assertEquals(Locale.CHINESE.getLanguage(), UserContextUtil.getLocale().getLanguage());
    }

    @Test
    @DisplayName("移除上下文后应恢复为默认对象")
    void shouldResetToDefaultsAfterRemove() {
        RequestContext requestContext = new RequestContext();
        requestContext.setLanguage("fr");
        UserContextUtil.setRequestContext(requestContext);
        UserContextUtil.removeRequestContext();
        UserContextUtil.removeUserClaim();

        assertNotNull(UserContextUtil.getRequestContext());
        assertNotNull(UserContextUtil.getUserClaim());
        assertEquals("en", UserContextUtil.getLanguageOrDefault());
    }

    @Test
    @DisplayName("当用户 ID 非数字时获取 long 用户 ID 应抛出异常")
    void shouldThrowWhenUserIdIsNotNumeric() {
        RequestContext requestContext = new RequestContext();
        requestContext.setUserIdFromGateway("non-numeric");
        UserContextUtil.setRequestContext(requestContext);

        assertThrows(NumberFormatException.class, UserContextUtil::getLongUserId);
    }
}
