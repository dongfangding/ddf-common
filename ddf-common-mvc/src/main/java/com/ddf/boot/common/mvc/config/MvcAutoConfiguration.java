package com.ddf.boot.common.mvc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 核心模块的自动注入$
 *
 * @author dongfang.ding
 * @since 2020/8/15 0015 17:59
 */
@Configuration
@ComponentScan(basePackages = {"com.ddf.boot.common.mvc"})
public class MvcAutoConfiguration {

}
