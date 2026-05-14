package com.ddf.boot.common.authentication.config;

import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.authentication.annotation.EnableAuthenticate;
import com.ddf.boot.common.authentication.filter.AuthenticateTokenFilter;
import com.ddf.boot.common.authentication.interfaces.TokenCustomizeCheckService;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import com.ddf.boot.common.authentication.interfaces.impl.DefaultTokenCheckServiceImpl;
import com.ddf.boot.common.core.authentication.TokenCache;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(AuthenticationAutoConfiguration.class)).withUserConfiguration(
            AuthenticationTestConfiguration.class);

    @Test
    void shouldRegisterAuthenticationBeansWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthenticateTokenFilter.class);
            assertThat(context).hasSingleBean(DefaultTokenCheckServiceImpl.class);
            assertThat(context).hasSingleBean(TokenCache.class);
        });
    }

    @Test
    void shouldBackOffWhenCustomTokenCustomizeCheckServiceProvided() {
        contextRunner.withBean(TokenCustomizeCheckService.class, () -> Mockito.mock(TokenCustomizeCheckService.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenCustomizeCheckService.class);
                    assertThat(context).doesNotHaveBean(DefaultTokenCheckServiceImpl.class);
                });
    }

    @Test
    void shouldNotRegisterCoreBeansWithoutAuthenticateFilter() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AuthenticationAutoConfiguration.class))
                .withBean(UserClaimService.class, () -> (request, userClaim) -> UserClaim.getDefaultUser())
                .withBean(StringRedisTemplate.class, () -> Mockito.mock(StringRedisTemplate.class))
                .withBean(EnvironmentHelper.class, () -> Mockito.mock(EnvironmentHelper.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DefaultTokenCheckServiceImpl.class);
                    assertThat(context).doesNotHaveBean(TokenCache.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAuthenticate
    static class AuthenticationTestConfiguration {

        @Bean
        UserClaimService userClaimService() {
            return (request, userClaim) -> UserClaim.getDefaultUser();
        }

        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }

        @Bean
        EnvironmentHelper environmentHelper() {
            return Mockito.mock(EnvironmentHelper.class);
        }
    }
}
