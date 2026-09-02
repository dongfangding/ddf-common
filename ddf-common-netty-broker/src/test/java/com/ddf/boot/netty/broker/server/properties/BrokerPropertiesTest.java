package com.ddf.boot.netty.broker.server.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BrokerProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class BrokerPropertiesTest {

    @Test
    @DisplayName("应提供默认 broker 配置")
    void shouldProvideDefaultBrokerProperties() {
        BrokerProperties properties = new BrokerProperties();

        assertEquals(8888, properties.getPort());
        assertFalse(properties.isSsl());
        assertEquals(1048576, properties.getSoSndBuf());
        assertEquals(1048576, properties.getSoRecBuf());
        assertEquals(60, properties.getAllowIdleSeconds());
    }

    @Test
    @DisplayName("应支持覆盖 broker 配置")
    void shouldAllowOverrideBrokerProperties() {
        BrokerProperties properties = new BrokerProperties();

        properties.setPort(9999);
        properties.setSsl(true);
        properties.setSoSndBuf(2048);
        properties.setSoRecBuf(4096);
        properties.setAllowIdleSeconds(30);

        assertEquals(9999, properties.getPort());
        assertTrue(properties.isSsl());
        assertEquals(2048, properties.getSoSndBuf());
        assertEquals(4096, properties.getSoRecBuf());
        assertEquals(30, properties.getAllowIdleSeconds());
    }
}
