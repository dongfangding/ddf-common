package com.ddf.boot.common.authentication;

import com.ddf.boot.common.core.authentication.TokenGenerator;
import com.ddf.boot.common.core.config.CoreAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(CoreAutoConfiguration.class));

    @Test
    void tokenGenerator_default_bean_is_registered() {
        runner.run(ctx -> assertThat(ctx).hasSingleBean(TokenGenerator.class));
    }
}
