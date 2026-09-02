package com.ddf.boot.zookeeper.monitor.properties;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MonitorProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class MonitorPropertiesTest {

    @Test
    @DisplayName("应提供默认 zookeeper 监控配置")
    void shouldProvideDefaultMonitorProperties() {
        MonitorProperties properties = new MonitorProperties();

        assertEquals("127.0.0.1:2181", properties.getConnectAddress());
        assertEquals(60000, properties.getSessionTimeoutMs());
        assertEquals(15000, properties.getConnectionTimeoutMs());
        assertTrue(properties.getMonitors().isEmpty());
    }

    @Test
    @DisplayName("应支持覆盖监控端点列表")
    void shouldAllowOverrideMonitorNodes() {
        MonitorProperties properties = new MonitorProperties();
        MonitorNode node = new MonitorNode("10.0.0.1:2181", "/health", true);

        properties.setConnectAddress("10.0.0.2:2181");
        properties.setSessionTimeoutMs(30000);
        properties.setConnectionTimeoutMs(8000);
        properties.setMonitors(List.of(node));

        assertEquals("10.0.0.2:2181", properties.getConnectAddress());
        assertEquals(30000, properties.getSessionTimeoutMs());
        assertEquals(8000, properties.getConnectionTimeoutMs());
        assertEquals(1, properties.getMonitors().size());
        assertEquals("/health", properties.getMonitors().get(0).getMonitorPath());
        assertTrue(properties.getMonitors().get(0).isUseDefaultTimeStampUpload());
    }
}
