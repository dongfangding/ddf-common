package com.ddf.boot.common.data.mysql.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DataMysqlAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(DataMysqlAutoConfiguration.class));

    @Test
    void shouldBindDefaultPropertyWithoutGlobalSideEffect() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DataMysqlProperties.class);
            assertThat(System.getProperty("druid.mysql.usePingMethod")).isNull();
        });
    }

    @Test
    void shouldBindConfiguredPingMethodProperty() {
        contextRunner.withPropertyValues("customizer.data.mysql.druid.usePingMethod=true").run(context -> {
            DataMysqlProperties properties = context.getBean(DataMysqlProperties.class);
            assertThat(properties.getDruid().isUsePingMethod()).isTrue();
        });
    }

    @Test
    void shouldBackOffWhenModuleDisabled() {
        contextRunner.withPropertyValues("customizer.data.mysql.enabled=false").run(
                context -> assertThat(context).doesNotHaveBean(DataMysqlAutoConfiguration.class));
    }
}
