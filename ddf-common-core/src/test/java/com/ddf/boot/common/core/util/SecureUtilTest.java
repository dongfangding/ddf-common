package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.config.CoreAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SecureUtil} AES 加解密测试。
 * <p>AES 已改为 CBC 模式 + 随机 IV。带 key 的方法（{@code aesEncryptHexWithKey} /
 * {@code aesDecryptStrWithKey}）不依赖 Spring，可直接测试；不带 key 的方法
 * （{@code aesEncryptHex} / {@code aesDecryptStr}）通过 {@code GlobalProperties.aesSecret}
 * 读取密钥。</p>
 * <p>{@code SecureUtil} 在类加载时通过 {@code SpringContextHolder} 静态读取
 * {@code GlobalProperties}，且该静态字段 {@code static final} 仅初始化一次。因此依赖
 * Spring 的不带 key 测试必须最先执行（{@link Order} 保证），确保静态初始化时上下文已就绪
 * （参考 {@code DefaultTokenGeneratorTest}）。</p>
 *
 * @author snowball
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecureUtilTest {

    private static final String DATA = "snowball";
    private static final String AES_SECRET = "0123456789abcdef0123456789abcdef";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(CoreAutoConfiguration.class)).withUserConfiguration(
            RefreshScopeTestConfiguration.class).withPropertyValues(
            "customizer.infra.global-properties.aes-secret=" + AES_SECRET);

    @Test
    @Order(1)
    @DisplayName("不带 key 的 AES 加解密往返（依赖 GlobalProperties.aesSecret）")
    void aesEncryptAndDecryptWithoutKey() {
        contextRunner.run(context -> {
            assertThat(SecureUtil.aesDecryptStr(SecureUtil.aesEncryptHex(DATA))).isEqualTo(DATA);
        });
    }

    @Test
    @Order(2)
    @DisplayName("带 key 的 AES 加解密往返（无需 Spring）")
    void aesEncryptAndDecryptWithKey() {
        assertThat(SecureUtil.aesDecryptStrWithKey(SecureUtil.aesEncryptHexWithKey(DATA, AES_SECRET),
                AES_SECRET)).isEqualTo(DATA);
    }

    @AfterEach
    void cleanSpringUtil() {
        try {
            Class<?> clazz = Class.forName("cn.hutool.extra.spring.SpringUtil");
            for (String fieldName : new String[] {"beanFactory", "applicationContext"}) {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(null, null);
            }
        } catch (Exception e) {
            throw new IllegalStateException("无法重置 SpringUtil 静态上下文", e);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class RefreshScopeTestConfiguration {

        @Bean
        static BeanFactoryPostProcessor registerRefreshScope() {
            return beanFactory -> beanFactory.registerScope("refresh", new SimpleThreadScope());
        }
    }
}
