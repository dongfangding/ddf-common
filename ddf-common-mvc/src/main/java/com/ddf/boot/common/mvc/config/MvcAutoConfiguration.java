package com.ddf.boot.common.mvc.config;

import com.ddf.boot.common.core.config.GlobalProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.ddf.boot.common.mvc.controllerwrapper.CommonResponseBodyAdviceProperties;
import com.ddf.boot.common.mvc.exception200.CommonExceptionAdvice;
import com.ddf.boot.common.mvc.permissionscan.PermissionMenuScanner;
import com.ddf.boot.common.mvc.requestsign.RequestSignAccessFilterChain;

/**
 * 核心模块的自动注入
 *
 * @author dongfang.ding
 * @since 2020/8/15 0015 17:59
 */
@AutoConfiguration
@EnableConfigurationProperties(CommonResponseBodyAdviceProperties.class)
@Import(CoreWebConfig.class)
public class MvcAutoConfiguration {

    @Bean
    public PermissionMenuScanner permissionMenuScanner(ApplicationContext applicationContext) {
        return new PermissionMenuScanner(applicationContext);
    }

    @Bean
    public RequestSignAccessFilterChain requestSignAccessFilterChain(GlobalProperties globalProperties) {
        return new RequestSignAccessFilterChain(globalProperties);
    }

    @Bean
    public CommonExceptionAdvice commonExceptionAdvice() {
        return new CommonExceptionAdvice();
    }
}
