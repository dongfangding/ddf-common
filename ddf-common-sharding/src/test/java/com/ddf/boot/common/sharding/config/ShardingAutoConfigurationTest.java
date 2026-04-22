package com.ddf.boot.common.sharding.config;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.sharding.prop.SpringBootPropertiesConfiguration;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * ShardingAutoConfiguration 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class ShardingAutoConfigurationTest {

    @Test
    @DisplayName("未配置 mode 时 modeConfiguration 应返回 null")
    void shouldReturnNullModeConfigurationWhenModeNotConfigured() {
        SpringBootPropertiesConfiguration props = new SpringBootPropertiesConfiguration();
        ShardingAutoConfiguration configuration = new ShardingAutoConfiguration(props);

        assertNull(configuration.modeConfiguration());
    }

    @Test
    @DisplayName("本地规则和数据源都为空时应返回空数据源")
    void shouldReturnNullShardingSphereDataSourceWhenNoRulesAndDataSources() throws SQLException {
        SpringBootPropertiesConfiguration props = new SpringBootPropertiesConfiguration();
        props.setProps(new Properties());
        ShardingAutoConfiguration configuration = new ShardingAutoConfiguration(props);

        DataSource dataSource = configuration.shardingSphereDataSource(
                provider(Collections.emptyMap()),
                provider(new ArrayList<RuleConfiguration>()),
                provider(null)
        );

        assertNull(dataSource);
    }

    @Test
    @DisplayName("缺少数据源且没有 mode 时兜底 dataSource 应返回 null")
    void shouldReturnNullFallbackDataSourceWhenNothingConfigured() throws SQLException {
        SpringBootPropertiesConfiguration props = new SpringBootPropertiesConfiguration();
        props.setProps(new Properties());
        ShardingAutoConfiguration configuration = new ShardingAutoConfiguration(props);

        DataSource dataSource = configuration.dataSource(
                provider(Collections.emptyMap()),
                provider(null)
        );

        assertNull(dataSource);
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        when(provider.getIfAvailable(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            java.util.function.Supplier<T> supplier = invocation.getArgument(0);
            return value != null ? value : supplier.get();
        });
        return provider;
    }
}
