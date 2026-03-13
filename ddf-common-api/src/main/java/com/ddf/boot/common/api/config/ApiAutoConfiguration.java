package com.ddf.boot.common.api.config;

import com.ddf.boot.common.api.context.EnvironmentContext;
import com.ddf.boot.common.api.sensitive.SensitiveInfoSerialize;
import com.ddf.boot.common.api.urlreplace.StaticProperties;
import com.ddf.boot.common.api.urlreplace.UrlReplaceDeserialize;
import com.ddf.boot.common.api.urlreplace.UrlReplaceHelper;
import com.ddf.boot.common.api.urlreplace.UrlReplaceSerialize;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * api模块的自动注入
 *
 * @author dongfang.ding
 * @since 2020/8/15 0015 17:59
 */
@AutoConfiguration
@EnableConfigurationProperties(StaticProperties.class)
@Import({EnvironmentContext.class, SensitiveInfoSerialize.class, UrlReplaceHelper.class,
        UrlReplaceSerialize.class, UrlReplaceDeserialize.class})
public class ApiAutoConfiguration {

}
