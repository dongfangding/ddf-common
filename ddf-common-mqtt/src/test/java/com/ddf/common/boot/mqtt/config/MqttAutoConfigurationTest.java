package com.ddf.common.boot.mqtt.config;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.common.boot.mqtt.client.MqttPublishClient;
import com.ddf.common.boot.mqtt.controller.EmqController;
import com.ddf.common.boot.mqtt.extra.impl.MqttPublishCheckerListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class MqttAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MqttAutoConfiguration.class))
            .withPropertyValues(
                    "customizer.infra.mqtt.enable=true",
                    "customizer.infra.mqtt.client.username=test",
                    "customizer.infra.mqtt.client.password=test",
                    "customizer.infra.mqtt.client.client-id-prefix=app",
                    "customizer.infra.mqtt.connection-urls[0].protocol=mqtt_tcp",
                    "customizer.infra.mqtt.connection-urls[0].url=tcp://127.0.0.1:1883"
            )
            .withUserConfiguration(MqttSupportConfiguration.class);

    @Test
    void shouldRegisterControllerAndPublishListener() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EmqController.class);
            assertThat(context).hasSingleBean(MqttPublishCheckerListener.class);
            assertThat(context).hasSingleBean(MqttPublishClient.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class MqttSupportConfiguration {

        @Bean
        EnvironmentHelper environmentHelper() {
            EnvironmentHelper helper = Mockito.mock(EnvironmentHelper.class);
            Mockito.when(helper.getPort()).thenReturn(8080);
            return helper;
        }
    }
}
