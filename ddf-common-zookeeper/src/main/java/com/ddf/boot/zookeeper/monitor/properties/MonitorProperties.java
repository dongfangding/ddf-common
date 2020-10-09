package com.ddf.boot.zookeeper.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/09 10:38
 */
@Data
@ConfigurationProperties(prefix = "customs.zookeeper.monitor")
@Component
public class MonitorProperties {

    private static final int DEFAULT_SESSION_TIMEOUT_MS = Integer.getInteger("curator-default-session-timeout", 60 * 1000);
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = Integer.getInteger("curator-default-connection-timeout", 15 * 1000);

    /**
     * zk服务器连接地址
     */
    private String connectAddress = "127.0.0.1:2181";

    /**
     * session timeout
     */
    private int sessionTimeoutMs = DEFAULT_SESSION_TIMEOUT_MS;

    /**
     * connection timeout
     */
    private int connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT_MS;

    /**
     * 监控端点对象集合
     */
    private List<MonitorNode> monitors;

}
