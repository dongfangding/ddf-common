package com.ddf.boot.common.data.mysql.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MySQL and Druid infrastructure auto configuration.
 *
 * @author dongfang.ding
 * @since 2026/3/10
 */
@AutoConfiguration
@EnableConfigurationProperties(DataMysqlProperties.class)
@ConditionalOnClass(
        name = {"javax.sql.DataSource", "com.mysql.cj.jdbc.Driver", "com.alibaba.druid.pool.DruidDataSource"})
@ConditionalOnProperty(prefix = "customizer.data.mysql", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataMysqlAutoConfiguration {

    private final DataMysqlProperties properties;

    public DataMysqlAutoConfiguration(DataMysqlProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        System.setProperty("druid.mysql.usePingMethod", String.valueOf(properties.getDruid().isUsePingMethod()));
    }
}
