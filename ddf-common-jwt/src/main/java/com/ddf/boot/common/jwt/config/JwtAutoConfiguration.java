package com.ddf.boot.common.jwt.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * jwt模块的自动配置类类
 *
 * @author dongfang.ding
 * @date 2020/8/16 0016 13:59
 */
@Configuration
@ComponentScan(basePackages = "com.ddf.boot.common.jwt")
public class JwtAutoConfiguration {
}
