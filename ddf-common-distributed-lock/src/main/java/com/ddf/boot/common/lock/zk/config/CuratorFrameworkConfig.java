package com.ddf.boot.common.lock.zk.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化curator框架
 *
 * @author dongfang.ding
 * @date 2020/3/13 0013 16:53
 **/
@Configuration
public class CuratorFrameworkConfig {

    @Autowired
    private DistributedLockZookeeperProperties distributedLockZookeeperProperties;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                distributedLockZookeeperProperties.getConnectString(),
                distributedLockZookeeperProperties.getSessionTimeoutMs(),
                distributedLockZookeeperProperties.getConnectionTimeoutMs(),
                new RetryNTimes(distributedLockZookeeperProperties.getRetryCount(),
                        distributedLockZookeeperProperties.getElapsedTimeMs()));
    }

}
