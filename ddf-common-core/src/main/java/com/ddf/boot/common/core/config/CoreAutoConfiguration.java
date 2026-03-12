package com.ddf.boot.common.core.config;

import com.ddf.boot.common.core.constant.GlobalConstants;
import com.ddf.boot.common.core.gracefulshutdown.ExecutorServiceGracefulShutdownDefinition;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 核心模块的自动注入
 *
 * @author dongfang.ding
 * @since 2020/8/15 0015 17:59
 */
@AutoConfiguration
@EnableConfigurationProperties(GlobalProperties.class)
@ComponentScan(basePackages = GlobalConstants.CORE_BASE_PACKAGE)
public class CoreAutoConfiguration {

    /**
     * 线程池优雅关闭注册类
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ExecutorServiceGracefulShutdownDefinition threadPoolExecutorShutdownDefinition() {
        return new ExecutorServiceGracefulShutdownDefinition(120, TimeUnit.SECONDS);
    }
}
