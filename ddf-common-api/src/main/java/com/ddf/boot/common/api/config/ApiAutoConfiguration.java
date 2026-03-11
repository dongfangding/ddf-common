package com.ddf.boot.common.api.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * api模块的自动注入
 *
 * @author dongfang.ding
 * @since 2020/8/15 0015 17:59
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.ddf.boot.common.api")
public class ApiAutoConfiguration {

}
