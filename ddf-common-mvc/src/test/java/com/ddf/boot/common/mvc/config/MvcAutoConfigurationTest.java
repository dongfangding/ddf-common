package com.ddf.boot.common.mvc.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.mvc.exception200.CommonExceptionAdvice;
import com.ddf.boot.common.mvc.permissionscan.PermissionMenuScanner;
import com.ddf.boot.common.mvc.requestsign.RequestSignAccessFilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * MvcAutoConfiguration 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class MvcAutoConfigurationTest {

    private final MvcAutoConfiguration autoConfiguration = new MvcAutoConfiguration();

    @Test
    @DisplayName("应能创建 PermissionMenuScanner 并正确持有 ApplicationContext")
    void shouldCreatePermissionMenuScannerWithApplicationContext() {
        ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
        PermissionMenuScanner scanner = autoConfiguration.permissionMenuScanner(mockContext);

        assertNotNull(scanner);
        assertInstanceOf(PermissionMenuScanner.class, scanner);
    }

    @Test
    @DisplayName("应能创建 RequestSignAccessFilterChain 并正确注入 GlobalProperties")
    void shouldCreateRequestSignAccessFilterChainWithGlobalProperties() {
        GlobalProperties globalProperties = new GlobalProperties();
        globalProperties.setSignSecret("test-secret");
        RequestSignAccessFilterChain filterChain = autoConfiguration.requestSignAccessFilterChain(globalProperties);

        assertNotNull(filterChain);
        assertInstanceOf(RequestSignAccessFilterChain.class, filterChain);
    }

    @Test
    @DisplayName("应能创建 CommonExceptionAdvice")
    void shouldCreateCommonExceptionAdvice() {
        CommonExceptionAdvice advice = autoConfiguration.commonExceptionAdvice();

        assertNotNull(advice);
        assertInstanceOf(CommonExceptionAdvice.class, advice);
    }
}
