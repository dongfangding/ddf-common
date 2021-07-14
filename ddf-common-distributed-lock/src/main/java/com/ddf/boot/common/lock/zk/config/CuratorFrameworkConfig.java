package com.ddf.boot.common.lock.zk.config;

import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.common.lock.zk.impl.ZookeeperDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化curator框架
 *
 * @author dongfang.ding
 * @date 2020/3/13 0013 16:53
 **/
@Configuration
@Slf4j
@EnableConfigurationProperties(value = {DistributedLockZookeeperProperties.class})
@ConditionalOnProperty(value = "distributed.lock.zookeeper.enable", havingValue = "true")
public class CuratorFrameworkConfig {

    private final DistributedLockZookeeperProperties distributedLockZookeeperProperties;

    public CuratorFrameworkConfig(DistributedLockZookeeperProperties distributedLockZookeeperProperties) {
        this.distributedLockZookeeperProperties = distributedLockZookeeperProperties;
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        log.info("开始初始化分布式锁zk客户端工具");
        return CuratorFrameworkFactory.newClient(
                distributedLockZookeeperProperties.getConnectString(),
                distributedLockZookeeperProperties.getSessionTimeoutMs(),
                distributedLockZookeeperProperties.getConnectionTimeoutMs(), new RetryNTimes(
                        distributedLockZookeeperProperties.getRetryCount(),
                        distributedLockZookeeperProperties.getElapsedTimeMs()
                )
        );
    }

    @Bean(name = ZookeeperDistributedLock.BEAN_NAME)
    public DistributedLock zookeeperDistributedLock() {
        return new ZookeeperDistributedLock(curatorFramework(), distributedLockZookeeperProperties);
    }
}
