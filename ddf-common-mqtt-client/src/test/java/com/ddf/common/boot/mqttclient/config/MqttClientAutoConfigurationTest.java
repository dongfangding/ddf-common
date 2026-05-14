package com.ddf.common.boot.mqttclient.config;

import com.ddf.common.boot.mqtt.client.MqttDefinition;
import com.ddf.common.boot.mqtt.client.MqttPublishClient;
import com.ddf.common.boot.mqttclient.controller.MqttClientController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class MqttClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(MqttClientAutoConfiguration.class)).withUserConfiguration(
            MqttClientSupportConfiguration.class);

    @Test
    void shouldRegisterControllerWhenPublishClientExists() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(MqttClientController.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class MqttClientSupportConfiguration {

        @Bean
        MqttPublishClient mqttPublishClient() {
            return new MqttPublishClient(Mockito.mock(MqttDefinition.class));
        }
    }
}
