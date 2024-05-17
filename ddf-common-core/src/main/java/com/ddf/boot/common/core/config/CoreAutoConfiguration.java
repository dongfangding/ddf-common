package com.ddf.boot.common.core.config;

import com.ddf.boot.common.core.constant.GlobalConstants;
import com.ddf.boot.common.core.encode.BCryptPasswordEncoder;
import com.ddf.boot.common.core.gracefulshutdown.ExecutorServiceGracefulShutdownDefinition;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 核心模块的自动注入$
 *
 * @author dongfang.ding
 * @date 2020/8/15 0015 17:59
 */
@Configuration
@ComponentScan(basePackages = GlobalConstants.CORE_BASE_PACKAGE)
public class CoreAutoConfiguration {


    @Bean
    @Primary
    @ConditionalOnMissingBean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
