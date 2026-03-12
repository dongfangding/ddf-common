package com.ddf.boot.common.governance.config;

import com.ddf.boot.common.governance.mail.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

class GovernanceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GovernanceAutoConfiguration.class))
            .withUserConfiguration(MailSupportConfiguration.class);

    @Test
    void shouldCreateMailServiceWhenMailDependenciesExist() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(MailService.class));
    }

    @Test
    void shouldNotCreateMailServiceWhenMailFeatureDisabled() {
        contextRunner
                .withPropertyValues("customizer.governance.mail.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MailService.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class MailSupportConfiguration {

        @Bean
        JavaMailSender javaMailSender() {
            return new JavaMailSenderImpl();
        }

        @Bean
        MailProperties mailProperties() {
            return new MailProperties();
        }
    }
}
