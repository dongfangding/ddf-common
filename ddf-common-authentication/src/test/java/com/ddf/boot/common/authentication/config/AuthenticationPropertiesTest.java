package com.ddf.boot.common.authentication.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AuthenticationProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class AuthenticationPropertiesTest {

    @Test
    @DisplayName("默认配置值应符合预期")
    void shouldExposeExpectedDefaultValues() {
        AuthenticationProperties properties = new AuthenticationProperties();

        assertTrue(properties.isAutoRegisterFilter());
        assertEquals("ACCESS-TOKEN", properties.getTokenHeaderName());
        assertEquals("", properties.getTokenPrefix());
        assertEquals(RequestHeaderEnum.IMEI.getName(), properties.getCreditHeaderName());
        assertTrue(properties.getIgnores().isEmpty());
        assertTrue(properties.getOpenIgnores().isEmpty());
        assertTrue(properties.isSignEnabled());
        assertEquals(10, properties.getTimeForceCheckDiffMinute());
    }
}
