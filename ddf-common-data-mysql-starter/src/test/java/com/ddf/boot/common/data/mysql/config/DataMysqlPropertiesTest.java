package com.ddf.boot.common.data.mysql.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DataMysqlProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class DataMysqlPropertiesTest {

    @Test
    @DisplayName("应提供默认数据源治理配置")
    void shouldProvideDefaultMysqlProperties() {
        DataMysqlProperties properties = new DataMysqlProperties();

        assertTrue(properties.isEnabled());
        assertFalse(properties.getDruid().isUsePingMethod());
    }

    @Test
    @DisplayName("应支持覆盖 Druid ping 配置")
    void shouldAllowOverrideDruidProperties() {
        DataMysqlProperties properties = new DataMysqlProperties();

        properties.setEnabled(false);
        properties.getDruid().setUsePingMethod(true);

        assertFalse(properties.isEnabled());
        assertTrue(properties.getDruid().isUsePingMethod());
    }
}
