package com.ddf.boot.common.ids.config;

import com.ddf.boot.common.ids.helper.SnowflakeServiceHelper;
import com.sankuai.inf.leaf.plugin.annotation.EnableLeafServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 如果全局id采用引用当前模块的方式，那么就需要实现自动注入的功能$
 *
 * @author dongfang.ding
 * @date 2020/8/15 0015 17:54
 */
@Configuration
@EnableLeafServer
public class IdsAutoConfiguration {

    @Bean
    public SnowflakeServiceHelper snowflakeServiceHelper() {
        return new SnowflakeServiceHelper();
    }
}
