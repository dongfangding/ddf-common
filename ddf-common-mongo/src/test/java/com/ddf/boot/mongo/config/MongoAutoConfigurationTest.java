package com.ddf.boot.mongo.config;

import com.ddf.boot.mongo.helper.MongoTemplateHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class MongoAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(MongoAutoConfiguration.class)).withUserConfiguration(MongoSupportConfiguration.class);

    @Test
    void shouldRegisterMongoTemplateHelperWhenMongoTemplateExists() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(MongoTemplateHelper.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class MongoSupportConfiguration {

        @Bean
        MongoTemplate mongoTemplate() {
            return Mockito.mock(MongoTemplate.class);
        }
    }
}
