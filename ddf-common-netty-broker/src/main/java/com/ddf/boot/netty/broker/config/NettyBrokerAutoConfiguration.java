package com.ddf.boot.netty.broker.config;

import com.ddf.boot.netty.broker.server.properties.BrokerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * netty-broker自动配置类$
 *
 * @author dongfang.ding
 * @since 2020/9/21 0021 23:37
 */
@AutoConfiguration
@EnableConfigurationProperties(BrokerProperties.class)
public class NettyBrokerAutoConfiguration {
}
