package com.ddf.boot.common.core.config;

import com.ddf.boot.common.core.constant.GlobalConstants;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 核心模块的自动注入$
 *
 * @author dongfang.ding
 * @date 2020/8/15 0015 17:59
 */
@Configuration
@ComponentScan(basePackages = GlobalConstants.CORE_BASE_PACKAGE)
public class CoreAutoConfiguration {
}
