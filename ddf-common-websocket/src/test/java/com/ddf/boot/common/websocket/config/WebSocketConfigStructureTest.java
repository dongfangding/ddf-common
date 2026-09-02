package com.ddf.boot.common.websocket.config;

import com.ddf.boot.common.websocket.handler.impl.HandlerMessageServiceImpl;
import com.ddf.boot.common.websocket.helper.CmdStrategyHelper;
import com.ddf.boot.common.websocket.interceptor.RSAEncryptProcessor;
import com.ddf.boot.common.websocket.listeners.RedirectCmdListener;
import com.ddf.boot.common.websocket.listeners.RemoveOfflineKeyListener;
import com.ddf.boot.common.websocket.listeners.ServerNodeOfflineListener;
import com.ddf.boot.common.websocket.properties.WebSocketProperties;
import com.ddf.boot.common.websocket.service.impl.ChannelTransferServiceImpl;
import com.ddf.boot.common.websocket.service.impl.WsMessageServiceImpl;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * WebSocket 自动配置结构测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class WebSocketConfigStructureTest {

    @Test
    @DisplayName("WebSocketConfig 不再使用 ComponentScan")
    void shouldNotUseComponentScan() {
        assertFalse(WebSocketConfig.class.isAnnotationPresent(ComponentScan.class));
    }

    @Test
    @DisplayName("websocket 具体类不再依赖 stereotype 扫描注册")
    void shouldNotUseStereotypeAnnotations() {
        assertNotAnnotatedWith(WebSocketProperties.class, Component.class);
        assertNotAnnotatedWith(RSAEncryptProcessor.class, Component.class);
        assertNotAnnotatedWith(CmdStrategyHelper.class, Component.class);
        assertNotAnnotatedWith(RedirectCmdListener.class, Component.class);
        assertNotAnnotatedWith(RemoveOfflineKeyListener.class, Component.class);
        assertNotAnnotatedWith(ServerNodeOfflineListener.class, Component.class);
        assertNotAnnotatedWith(ChannelTransferServiceImpl.class, Service.class);
        assertNotAnnotatedWith(HandlerMessageServiceImpl.class, Service.class);
        assertNotAnnotatedWith(WsMessageServiceImpl.class, Service.class);
    }

    private void assertNotAnnotatedWith(Class<?> type, Class<? extends Annotation> annotationType) {
        assertFalse(type.isAnnotationPresent(annotationType),
                () -> type.getName() + " 不应继续使用 @" + annotationType.getSimpleName());
    }
}
