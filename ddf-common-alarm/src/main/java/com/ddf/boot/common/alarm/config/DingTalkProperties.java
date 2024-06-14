package com.ddf.boot.common.alarm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/06/04 14:56
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "customs.alarm.dingtalk")
public class DingTalkProperties {

    /**
     * 业务告警-资源告警机器人
     */
   private Properties bizResource;

    /**
     * 业务告警-代码异常告警机器人
     */
   private Properties codeException;

    @Data
    public static class Properties {

        /**
         * 访问token
         */
        private String accessToken;

        /**
         * 加签密钥
         */
        private String secret;
    }
}
