package com.ddf.boot.common.ext;

import com.ddf.boot.common.ext.oss.config.OssBeanAutoConfiguration;
import com.ddf.boot.common.ext.oss.config.OssProperties;
import com.ddf.boot.common.ext.sms.SmsApi;
import com.ddf.boot.common.ext.sms.aliyun.AliYunSmsApiImpl;
import com.ddf.boot.common.ext.sms.aliyun.config.AliYunSmsProperties;
import com.ddf.boot.common.ext.sms.aliyun.helper.AliYunSmsHelper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * <p>ext包的自动配置类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/10/12 13:35
 */
@AutoConfiguration
@EnableConfigurationProperties({AliYunSmsProperties.class, OssProperties.class})
@Import(value = {OssBeanAutoConfiguration.class})
public class ExtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AliYunSmsHelper.class)
    public AliYunSmsHelper aliYunSmsHelper(AliYunSmsProperties aliYunSmsProperties) {
        return new AliYunSmsHelper(aliYunSmsProperties);
    }

    @Bean
    @ConditionalOnMissingBean(SmsApi.class)
    public SmsApi smsApi(AliYunSmsHelper aliYunSmsHelper) {
        return new AliYunSmsApiImpl(aliYunSmsHelper);
    }
}
