package com.ddf.boot.common.xxl.executor.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;

import static com.ddf.boot.common.xxl.executor.config.XxlJobConfig.APP_NAME_AUTO;

/**
 * <p>description</p >
 *
 * https://www.xuxueli.com/xxl-job/#%E3%80%8A%E5%88%86%E5%B8%83%E5%BC%8F%E4%BB%BB%E5%8A%A1%E8%B0%83%E5%BA%A6%E5%B9%B3%E5%8F%B0XXL-JOB%E3%80%8B
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/12/07 17:03
 */
@Configuration
@EnableConfigurationProperties({XxlJobConfig.class})
public class XxlJobAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(XxlJobAutoConfiguration.class);

    private final XxlJobConfig xxlJobConfig;

    private final Environment environment;

    public XxlJobAutoConfiguration(XxlJobConfig xxlJobConfig, Environment environment) {
        this.xxlJobConfig = xxlJobConfig;
        this.environment = environment;
    }

    /**
     * 注册执行器
     * @return
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        logger.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobConfig.getAdminAddresses());

        xxlJobSpringExecutor.setAppname(getAppName());
        xxlJobSpringExecutor.setAddress(xxlJobConfig.getAddress());
        xxlJobSpringExecutor.setIp(xxlJobConfig.getIp());
        xxlJobSpringExecutor.setPort(xxlJobConfig.getPort());
        xxlJobSpringExecutor.setAccessToken(xxlJobConfig.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlJobConfig.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobConfig.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }

    /**
     * 获取app name  保留了原设计中为空的含义， 只有为auto时才使用application.name
     * @return
     */
    private String getAppName() {
        String appName = xxlJobConfig.getAppName();
        if (appName != null && Objects.equals(APP_NAME_AUTO, appName)) {
            return environment.getProperty("spring.application.name");
        }
        return appName;
    }
}
