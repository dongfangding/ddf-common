package com.ddf.boot.common.rocketmq.config;

import com.ddf.boot.common.rocketmq.producer.RocketProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class RocketMQEnhanceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(RocketMQEnhanceAutoConfiguration.class)).withUserConfiguration(
            RocketMQSupportConfiguration.class);

    @Test
    void shouldRegisterRocketProducerWhenTemplateExists() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(RocketProducer.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class RocketMQSupportConfiguration {

        @Bean
        RocketMQTemplate rocketMQTemplate() {
            return Mockito.mock(RocketMQTemplate.class);
        }
    }
}
