package com.ddf.boot.common.websocket;

import com.ddf.boot.common.websocket.config.WebSocketConfig;
import com.ddf.boot.common.websocket.helper.CmdStrategyHelper;
import com.ddf.boot.common.websocket.listeners.RedirectCmdListener;
import com.ddf.boot.common.websocket.listeners.RemoveOfflineKeyListener;
import com.ddf.boot.common.websocket.listeners.ServerNodeOfflineListener;
import com.ddf.boot.common.websocket.service.impl.WsMessageServiceImpl;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * websocket 模块注入风格测试
 *
 * @author Codex
 * @since 2026/03/13
 */
class InjectionStyleTest {

    @Test
    @DisplayName("websocket 关键类不再使用字段 Autowired 注入")
    void shouldNotUseFieldAutowiredInWebsocketComponents() {
        assertNoAutowiredField(WebSocketConfig.class);
        assertNoAutowiredField(WsMessageServiceImpl.class);
        assertNoAutowiredField(RedirectCmdListener.class);
        assertNoAutowiredField(RemoveOfflineKeyListener.class);
        assertNoAutowiredField(ServerNodeOfflineListener.class);
        assertNoAutowiredField(CmdStrategyHelper.class);
    }

    @Test
    @DisplayName("websocket 关键类应提供明确构造函数")
    void shouldExposeExplicitConstructors() {
        assertHasNonDefaultConstructor(WebSocketConfig.class);
        assertHasNonDefaultConstructor(WsMessageServiceImpl.class);
        assertHasNonDefaultConstructor(RedirectCmdListener.class);
        assertHasNonDefaultConstructor(RemoveOfflineKeyListener.class);
        assertHasNonDefaultConstructor(ServerNodeOfflineListener.class);
        assertHasNonDefaultConstructor(CmdStrategyHelper.class);
    }

    private void assertNoAutowiredField(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            assertFalse(field.isAnnotationPresent(Autowired.class),
                    () -> type.getName() + " 仍然存在字段 @Autowired: " + field.getName());
        }
    }

    private void assertHasNonDefaultConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        assertTrue(Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() > 0),
                () -> type.getName() + " 应提供参数化构造函数");
        assertEquals(1, constructors.length, () -> type.getName() + " 应只保留一个构造函数以明确依赖");
    }
}
