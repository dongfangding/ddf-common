package com.ddf.boot.common.lock.zk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * zookeeper客户端配置
 *
 * @author dongfang.ding
 * @date 2020/3/13 0013 16:43
 **/
@ConfigurationProperties(prefix = "customs.distributed.lock.zookeeper")
@Data
public class DistributedLockZookeeperProperties {

    /**
     * 是否启用分布式锁
     */
    private boolean enable;

    /**
     * 根默认
     */
    private String root = "/distributed_lock";

    /**
     * zookeeper的连接地址
     */
    private String connectString = "127.0.0.1:2181";

    /**
     * session超时时间 ms
     */
    private Integer sessionTimeoutMs = 60000;

    /**
     * 连接超时 ms
     */
    private Integer connectionTimeoutMs = 10000;

    /**
     * 重试次数
     */
    private int retryCount = 3;

    /**
     * 重试间隔 ms
     */
    private int elapsedTimeMs = 2000;

}
