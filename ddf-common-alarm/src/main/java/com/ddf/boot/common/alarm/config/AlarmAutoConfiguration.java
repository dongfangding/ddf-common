package com.ddf.boot.common.alarm.config;

import com.ddf.boot.common.alarm.notify.CodeExceptionNotify;
import com.ddf.boot.common.alarm.notify.TableNotifyImpl;
import com.ddf.boot.common.alarm.rule.tablescan.TableNotify;
import com.ddf.boot.common.alarm.rule.tablescan.TableScan;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2024/06/04 14:08
 */
@AutoConfiguration
@Import(AlarmThreadConfig.class)
@EnableConfigurationProperties({DingTalkProperties.class, ExceptionAlarmProperties.class, LarkProperties.class})
public class AlarmAutoConfiguration {

    @Bean
    public CodeExceptionNotify codeExceptionNotify(ThreadPoolTaskExecutor globalExceptionExecutor,
            DingTalkProperties dingTalkProperties, ExceptionAlarmProperties exceptionAlarmProperties,
            LarkProperties larkProperties, EnvironmentHelper environmentHelper) {
        return new CodeExceptionNotify(globalExceptionExecutor, dingTalkProperties, exceptionAlarmProperties,
                larkProperties, environmentHelper);
    }

    @Bean
    public TableNotify tableNotify(DingTalkProperties dingTalkProperties, LarkProperties larkProperties,
            SmartInitializingSingleton loadBalancedAsyncRestTemplateInitializer) {
        return new TableNotifyImpl(dingTalkProperties, larkProperties, loadBalancedAsyncRestTemplateInitializer);
    }

    @Bean
    public TableScan tableScan(Environment environment, Optional<DataSourceProperties> dataSourceProperties,
            Optional<TableNotify> tableNotify, Optional<DataSource> dataSource) {
        return new TableScan(environment, dataSourceProperties, tableNotify, dataSource);
    }
}
