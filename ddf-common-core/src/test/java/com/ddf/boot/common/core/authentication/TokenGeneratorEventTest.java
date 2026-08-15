package com.ddf.boot.common.core.authentication;

import cn.hutool.crypto.asymmetric.RSA;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.core.config.CoreAutoConfiguration;
import com.ddf.boot.common.core.event.LoginSuccessEvent;
import com.ddf.boot.common.core.event.TokenRefreshEvent;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.SimpleThreadScope;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证生命周期事件发布测试。
 *
 * <p>验证 {@link DefaultTokenGenerator} 在 createToken / refreshToken 时分别发布
 * {@link LoginSuccessEvent} / {@link TokenRefreshEvent}。</p>
 */
class TokenGeneratorEventTest {

    private static final String AES_SECRET = "0123456789abcdef0123456789abcdef";
    private static final String RSA_PRIVATE_KEY;
    private static final String RSA_PUBLIC_KEY;

    static {
        RSA rsa = new RSA();
        RSA_PRIVATE_KEY = rsa.getPrivateKeyBase64();
        RSA_PUBLIC_KEY = rsa.getPublicKeyBase64();
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class))
            .withUserConfiguration(EventListenerTestConfiguration.class)
            .withPropertyValues(
                    "customizer.infra.global-properties.aes-secret=" + AES_SECRET,
                    "customizer.infra.global-properties.rsa-private-key=" + RSA_PRIVATE_KEY,
                    "customizer.infra.global-properties.rsa-public-key=" + RSA_PUBLIC_KEY);

    @AfterEach
    void cleanSpringUtil() {
        SpringUtilCleaner.reset();
    }

    @Test
    void createTokenPublishesLoginSuccessEvent() {
        contextRunner.run(context -> {
            TokenGenerator tokenGenerator = context.getBean(TokenGenerator.class);
            EventListenerTestConfiguration listener = context.getBean(EventListenerTestConfiguration.class);

            tokenGenerator.createToken(UserClaim.mockUser("10086"));

            assertThat(listener.loginSuccessEvent).isNotNull();
            assertThat(listener.loginSuccessEvent.get().getUserClaim().getUserId()).isEqualTo("10086");
        });
    }

    @Test
    void refreshTokenPublishesTokenRefreshEvent() {
        contextRunner.run(context -> {
            TokenGenerator tokenGenerator = context.getBean(TokenGenerator.class);
            EventListenerTestConfiguration listener = context.getBean(EventListenerTestConfiguration.class);

            tokenGenerator.refreshToken("10086", "dummy-token");

            assertThat(listener.tokenRefreshEvent).isNotNull();
            assertThat(listener.tokenRefreshEvent.get().getUserId()).isEqualTo("10086");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class EventListenerTestConfiguration {

        final AtomicReference<LoginSuccessEvent> loginSuccessEvent = new AtomicReference<>();
        final AtomicReference<TokenRefreshEvent> tokenRefreshEvent = new AtomicReference<>();

        @Bean
        static BeanFactoryPostProcessor registerRefreshScope() {
            return beanFactory -> beanFactory.registerScope("refresh", new SimpleThreadScope());
        }

        @EventListener
        void onLoginSuccess(LoginSuccessEvent event) {
            loginSuccessEvent.set(event);
        }

        @EventListener
        void onTokenRefresh(TokenRefreshEvent event) {
            tokenRefreshEvent.set(event);
        }
    }
}
