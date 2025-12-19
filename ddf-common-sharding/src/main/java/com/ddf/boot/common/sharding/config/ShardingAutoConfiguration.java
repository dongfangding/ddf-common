package com.ddf.boot.common.sharding.config;


import com.ddf.boot.common.sharding.prop.SpringBootPropertiesConfiguration;
import com.ddf.boot.common.sharding.rule.LocalRulesCondition;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.spring.boot.datasource.DataSourceMapSetter;
import org.apache.shardingsphere.spring.boot.schema.DatabaseNameSetter;
import org.apache.shardingsphere.spring.transaction.TransactionTypeScanner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2024/06/03 19:26
 */
@Configuration
@ComponentScan(basePackages = {"org.apache.shardingsphere.spring.boot.converter", "com.ddf.boot.common.sharding"})
@EnableConfigurationProperties(SpringBootPropertiesConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@RequiredArgsConstructor
public class ShardingAutoConfiguration implements EnvironmentAware {
    private String databaseName;

    private final SpringBootPropertiesConfiguration props;

    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();

    /**
     * Get mode configuration.
     *
     * @return mode configuration
     */
    @Bean
    public ModeConfiguration modeConfiguration() {
        return null == props.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(props.getMode());
    }

    /**
     * Get ShardingSphere data source bean.
     *
     * @param rules rules configuration
     * @param modeConfig mode configuration
     * @return data source bean
     * @throws SQLException SQL exception
     */
    @Bean
    @Conditional(LocalRulesCondition.class)
    // ObjectProvider包装bean, 可以实现bean不存在不报错的效果，方法内部自己根据null兼容逻辑
    //    @Autowired(required = false) // springboot高版本强制报错这种写法
    public DataSource shardingSphereDataSource(final ObjectProvider<List<RuleConfiguration>> rules, final ObjectProvider<ModeConfiguration> modeConfig) throws SQLException {
        Collection<RuleConfiguration> ruleConfigs = Optional
                .ofNullable(rules.getIfAvailable()).orElseGet(Collections::emptyList);
        return ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig.getIfAvailable(), dataSourceMap, ruleConfigs, props.getProps());
    }

    /**
     * Get data source bean from registry center.
     *
     * @param modeConfig mode configuration
     * @return data source bean
     * @throws SQLException SQL exception
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(final ModeConfiguration modeConfig) throws SQLException {
        return !dataSourceMap.isEmpty() ? ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, Collections.emptyList(), props.getProps())
                : ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig);
    }

    /**
     * Create transaction type scanner.
     *
     * @return transaction type scanner
     */
    @Bean
    public TransactionTypeScanner transactionTypeScanner() {
        return new TransactionTypeScanner();
    }

    @Override
    public final void setEnvironment(final Environment environment) {
        dataSourceMap.putAll(DataSourceMapSetter.getDataSourceMap(environment));
        databaseName = DatabaseNameSetter.getDatabaseName(environment);
    }
}
