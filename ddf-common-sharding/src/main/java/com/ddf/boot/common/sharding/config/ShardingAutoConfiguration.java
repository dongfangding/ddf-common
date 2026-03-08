package com.ddf.boot.common.sharding.config;


import com.ddf.boot.common.sharding.prop.SpringBootPropertiesConfiguration;
import com.ddf.boot.common.sharding.rule.LocalRulesCondition;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * ShardingSphere 自动配置类.
 *
 * <p>注意: ShardingSphere 5.4.0 推荐使用 YAML 配置文件方式.
 * 此自动配置类用于从 YAML 配置文件加载 ShardingSphere 配置.</p>
 *
 * @author snowball
 * @version 1.0
 * @since 2024/06/03 19:26
 */
@Configuration
@EnableConfigurationProperties(SpringBootPropertiesConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class ShardingAutoConfiguration {

    private final SpringBootPropertiesConfiguration props;

    public ShardingAutoConfiguration(final SpringBootPropertiesConfiguration props) {
        this.props = props;
    }

    /**
     * 获取模式配置.
     *
     * @return 模式配置
     */
    @Bean
    public ModeConfiguration modeConfiguration() {
        return null == props.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(props.getMode());
    }

    /**
     * 获取 ShardingSphere 数据源 Bean.
     * <p>
     * 通过 YAML 配置文件方式创建数据源，配置文件通过 spring.datasource.url 指定.
     * </p>
     *
     * @param dataSourceMapProvider 数据源 Map Provider
     * @param rulesProvider         规则配置 Provider
     * @param modeConfigProvider    模式配置 Provider
     * @return 数据源 Bean
     * @throws SQLException SQL 异常
     */
    @Bean
    @Conditional(LocalRulesCondition.class)
    public DataSource shardingSphereDataSource(
            final ObjectProvider<Map<String, DataSource>> dataSourceMapProvider,
            final ObjectProvider<Collection<RuleConfiguration>> rulesProvider,
            final ObjectProvider<ModeConfiguration> modeConfigProvider) throws SQLException {
        Map<String, DataSource> dataSourceMap = dataSourceMapProvider.getIfAvailable(Collections::emptyMap);
        Collection<RuleConfiguration> ruleConfigs = Optional.ofNullable(rulesProvider.getIfAvailable())
                .orElseGet(ArrayList::new);
        ModeConfiguration modeConfiguration = modeConfigProvider.getIfAvailable();

        if (dataSourceMap.isEmpty() && ruleConfigs.isEmpty()) {
            return null;
        }

        // ShardingSphere 5.4.0 API: createDataSource(ModeConfiguration, Map, Collection, Properties)
        return ShardingSphereDataSourceFactory.createDataSource(
                modeConfiguration, dataSourceMap, ruleConfigs, props.getProps());
    }

    /**
     * 如果容器中没有 DataSource Bean，则创建 ShardingSphere 数据源.
     * <p>
     * 此 Bean 用于从注册中心或配置中心获取数据源配置.
     * </p>
     *
     * @param dataSourceMapProvider 数据源 Map Provider
     * @param modeConfigProvider    模式配置 Provider
     * @return 数据源 Bean
     * @throws SQLException SQL 异常
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(
            final ObjectProvider<Map<String, DataSource>> dataSourceMapProvider,
            final ObjectProvider<ModeConfiguration> modeConfigProvider) throws SQLException {
        Map<String, DataSource> dataSourceMap = dataSourceMapProvider.getIfAvailable(Collections::emptyMap);
        ModeConfiguration modeConfiguration = modeConfigProvider.getIfAvailable();

        if (dataSourceMap.isEmpty() && modeConfiguration == null) {
            return null;
        }

        if (dataSourceMap.isEmpty()) {
            return ShardingSphereDataSourceFactory.createDataSource(modeConfiguration);
        }

        // ShardingSphere 5.4.0 API: createDataSource(ModeConfiguration, Map, Collection, Properties)
        return ShardingSphereDataSourceFactory.createDataSource(
                modeConfiguration, dataSourceMap, Collections.emptyList(), props.getProps());
    }
}
