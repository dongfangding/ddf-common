package com.ddf.boot.common.core.authentication;

import cn.hutool.crypto.asymmetric.RSA;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.core.config.CoreAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultTokenGenerator} 的 createToken -> checkToken 往返测试。
 * <p>说明：{@code SecureUtil} 在类加载时通过 {@code SpringContextHolder} 静态读取
 * {@code GlobalProperties} 以初始化 RSA/AES 密钥，因此需要在 ApplicationContextRunner 中
 * 显式注册 {@code refresh} scope（{@code GlobalProperties} 标注了 {@code @RefreshScope}），
 * 并配置 AES/RSA 密钥。</p>
 */
class DefaultTokenGeneratorTest {

    private static final String AES_SECRET = "0123456789abcdef0123456789abcdef";
    private static final String RSA_PRIVATE_KEY;
    private static final String RSA_PUBLIC_KEY;

    static {
        RSA rsa = new RSA();
        RSA_PRIVATE_KEY = rsa.getPrivateKeyBase64();
        RSA_PUBLIC_KEY = rsa.getPublicKeyBase64();
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(CoreAutoConfiguration.class)).withUserConfiguration(
            RefreshScopeTestConfiguration.class).withPropertyValues(
            "customizer.infra.global-properties.aes-secret=" + AES_SECRET,
            "customizer.infra.global-properties.rsa-private-key=" + RSA_PRIVATE_KEY,
            "customizer.infra.global-properties.rsa-public-key=" + RSA_PUBLIC_KEY);

    @AfterEach
    void cleanSpringUtil() {
        SpringUtilCleaner.reset();
    }

    @Test
    void createTokenThenCheckTokenRoundTrip() {
        contextRunner.run(context -> {
            TokenGenerator tokenGenerator = context.getBean(TokenGenerator.class);

            UserClaim userClaim = UserClaim.mockUser("10086");
            String token = tokenGenerator.createToken(userClaim).getToken();

            assertThat(token).isNotBlank();

            UserClaim parsed = tokenGenerator.checkToken(token).getUserClaim();
            assertThat(parsed.getUserId()).isEqualTo("10086");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class RefreshScopeTestConfiguration {

        @Bean
        static BeanFactoryPostProcessor registerRefreshScope() {
            return beanFactory -> beanFactory.registerScope("refresh", new SimpleThreadScope());
        }
    }
}
