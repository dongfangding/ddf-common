package com.ddf.boot.common.rocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 火箭增强性能
 * rocketmq:
 *  enhance:
 *    # 启动隔离，用于激活配置类EnvironmentIsolationConfig
 *    # 启动后会自动在topic上拼接激活的配置文件，达到自动隔离的效果
 *    enabledIsolation: true
 *    # 隔离环境名称，拼接到topic后，topic_dev，默认空字符串
 *    environment: dev
 * @author YiMing
 * @since 2023/10/09
 */
@Data
@ConfigurationProperties(prefix = "rocketmq.enhance")
public class RocketEnhanceProperties {

    /**
     * 是否启用隔离
     */
    private boolean enabledIsolation;

    /**
     * 环境
     */
    private String environment;
}
