package com.ddf.boot.common.core.config;

import com.ddf.boot.common.core.authentication.DefaultTokenGenerator;
import com.ddf.boot.common.core.authentication.TokenCache;
import com.ddf.boot.common.core.authentication.TokenGenerator;
import com.ddf.boot.common.core.gracefulshutdown.ExecutorServiceGracefulShutdownDefinition;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.promise.CompletableFutureHelper;
import com.ddf.boot.common.core.promise.DeferredHelper;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * 核心模块的自动注入
 *
 * @author dongfang.ding
 * @since 2020/8/15 0015 17:59
 */
@AutoConfiguration
@EnableConfigurationProperties(GlobalProperties.class)
@Import(SpringContextHolder.class)
public class CoreAutoConfiguration {

    /**
     * 线程池优雅关闭注册类
     */
    @Bean
    @ConditionalOnMissingBean
    public ExecutorServiceGracefulShutdownDefinition threadPoolExecutorShutdownDefinition() {
        return new ExecutorServiceGracefulShutdownDefinition(120, TimeUnit.SECONDS);
    }

    @Bean
    @ConditionalOnMissingBean
    public EnvironmentHelper environmentHelper(Environment environment) {
        return new EnvironmentHelper(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public DeferredHelper<?, ?, ?> deferredHelper() {
        return new DeferredHelper<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public CompletableFutureHelper<?> completableFutureHelper() {
        return new CompletableFutureHelper<>();
    }

    @Bean
    @ConditionalOnMissingBean(TokenGenerator.class)
    public TokenGenerator tokenGenerator(ObjectProvider<TokenCache> tokenCacheProvider) {
        return new DefaultTokenGenerator(tokenCacheProvider);
    }
}
