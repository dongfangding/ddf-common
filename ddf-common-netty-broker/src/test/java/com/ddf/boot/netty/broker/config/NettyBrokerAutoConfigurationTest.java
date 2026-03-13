package com.ddf.boot.netty.broker.config;

import com.ddf.boot.netty.broker.server.properties.BrokerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class NettyBrokerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NettyBrokerAutoConfiguration.class))
            .withPropertyValues("netty-broker.port=9999");

    @Test
    void shouldRegisterBrokerProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(BrokerProperties.class);
            assertThat(context.getBean(BrokerProperties.class).getPort()).isEqualTo(9999);
        });
    }
}
