package com.ddf.boot.zookeeper;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.zookeeper.listener.NodeEventListener;
import com.ddf.boot.zookeeper.monitor.config.MonitorRegistryConfig;
import com.ddf.boot.zookeeper.monitor.properties.MonitorProperties;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/10/09 18:24
 */
@AutoConfiguration
@EnableConfigurationProperties(MonitorProperties.class)
public class ZookeeperAutoConfiguration {

    @Bean
    public MonitorRegistryConfig monitorRegistryConfig(MonitorProperties monitorProperties,
            EnvironmentHelper environmentHelper, List<NodeEventListener> nodeEventListeners,
            @Qualifier("zookeeperDistributedLock") DistributedLock zookeeperDistributedLock) {
        return new MonitorRegistryConfig(monitorProperties, environmentHelper, nodeEventListeners,
                zookeeperDistributedLock);
    }
}
