package com.ddf.boot.common.redis.config;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>简单的基于redisson去创建多redis客户端，注意只支持单机连接方式</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2023/10/06 18:09
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "customs.redis.extra-multi")
public class ExtraRedisProperties {

    /**
     * 是否开启
     */
    private boolean enable;

    private Map<String, RedisProperties> map;


    @Data
    public static class RedisProperties {
        private int database = 0;
        private String url;
        private String host = "localhost";
        private String username;
        private String password;
        private int port = 6379;
        private boolean ssl;
        private String clientName;
        private int timeout = 3000;
    }
}
