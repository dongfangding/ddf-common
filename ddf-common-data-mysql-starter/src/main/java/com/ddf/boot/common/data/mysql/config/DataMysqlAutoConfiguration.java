package com.ddf.boot.common.data.mysql.config;

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
}
