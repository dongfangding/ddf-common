package com.ddf.boot.common.governance.config;

import com.ddf.boot.common.governance.mail.DefaultMailService;
import com.ddf.boot.common.governance.mail.MailService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Governance starter entry point.
 * <p>This starter must remain safe to import with zero mail-specific
 * configuration. Feature-specific beans should be guarded by conditions.
 *
 * @author dongfang.ding
 * @since 2026/3/10
 */
@AutoConfiguration
@EnableConfigurationProperties(GovernanceProperties.class)
public class GovernanceAutoConfiguration {

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "customizer.governance.observability", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    public ThreadPoolMetricsBinder threadPoolMetricsBinder(ApplicationContext applicationContext,
            MeterRegistry meterRegistry, GovernanceProperties governanceProperties) {
        return new ThreadPoolMetricsBinder(applicationContext, meterRegistry, governanceProperties);
    }

    @Bean
    @ConditionalOnClass(JavaMailSender.class)
    @ConditionalOnBean({JavaMailSender.class, MailProperties.class})
    @ConditionalOnProperty(prefix = "customizer.governance.mail", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    public MailService mailService(JavaMailSender javaMailSender, MailProperties mailProperties) {
        return new DefaultMailService(javaMailSender, mailProperties);
    }
}
