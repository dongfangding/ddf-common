package com.ddf.boot.common.lock.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/13 20:10
 */
@ConfigurationProperties(prefix = "customs.distributed.lock.redis")
@Data
public class DistributedLockRedisProperties {

    /**
     * 是否启用redis分布式锁
     */
    private boolean enable;

    /**
     * 默认锁定时间, 单位毫秒
     */
    private Integer defaultLockTimeMillions = 1000;
}
