package com.ddf.boot.common.data.mysql.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DataMysqlAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DataMysqlAutoConfiguration.class));

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty("druid.mysql.usePingMethod");
    }

    @Test
    void shouldBindDefaultPropertyAndSetSystemProperty() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DataMysqlProperties.class);
            assertThat(System.getProperty("druid.mysql.usePingMethod")).isEqualTo("false");
        });
    }

    @Test
    void shouldApplyConfiguredPingMethodProperty() {
        contextRunner
                .withPropertyValues("customizer.data.mysql.druid.usePingMethod=true")
                .run(context -> assertThat(System.getProperty("druid.mysql.usePingMethod")).isEqualTo("true"));
    }

    @Test
    void shouldBackOffWhenModuleDisabled() {
        contextRunner
                .withPropertyValues("customizer.data.mysql.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(DataMysqlAutoConfiguration.class));
    }
}
