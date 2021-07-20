package com.ddf.common.ids.service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>雪花id配置类</p >
 *
 * @author Mitchell
 * @version 1.0
 * @date 2021/02/23 14:46
 */
@Data
@ConfigurationProperties(prefix = "customs.ids.snowflake")
public class SnowflakeProperties {

    /**
     * zookeeper连接地址， 支持集群
     */
    private String zkAddress;

    /**
     * 这个port并不参与实际的连接，只是监视客户端连接时使用ip和端口标识一个机器， 如果是一台机器伪集群时可以使用这个port区别
     */
    private Integer zkPort = 2181;

}
