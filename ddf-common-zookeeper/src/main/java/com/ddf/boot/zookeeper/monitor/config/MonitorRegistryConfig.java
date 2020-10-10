package com.ddf.boot.zookeeper.monitor.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.lock.DistributedLock;
import com.ddf.boot.zookeeper.listener.NodeEventListener;
import com.ddf.boot.zookeeper.monitor.properties.MonitorNode;
import com.ddf.boot.zookeeper.monitor.properties.MonitorProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/09 10:51
 */
@Component
@Slf4j
public class MonitorRegistryConfig implements InitializingBean {

    @Autowired
    private MonitorProperties monitorProperties;
    @Autowired
    private CuratorFramework client;
    @Autowired
    private EnvironmentHelper environmentHelper;
    @Autowired(required = false)
    private List<NodeEventListener> nodeEventListeners;
    @Autowired
    @Qualifier("zookeeperDistributedLock")
    private DistributedLock zookeeperDistributedLock;

    private final static String DATA_SPLIT_CHAR = ",";

    /**
     * 节点监控分布式锁路径
     */
    private static final String CHECK_NODE_LOCK_PATH = "/MONITOR_NODE_CHECK";


    /**
     * 连接客户端，注册CuratorFramework对象
     * http://curator.apache.org/getting-started.html
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework client() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        return CuratorFrameworkFactory.newClient(monitorProperties.getConnectAddress(),
                monitorProperties.getSessionTimeoutMs(), monitorProperties.getConnectionTimeoutMs(), retryPolicy);
    }

    /**
     * 初始化节点创建以及事件监听
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<MonitorNode> monitors = monitorProperties.getMonitors();
        if (CollUtil.isEmpty(monitors)) {
            return;
        }
        String path;
        for (MonitorNode monitor : monitors) {
            path = getMonitorPath(monitor);
            // 创建节点
            createNode(path, monitor);

            // 监听节点事件
            listenerNode(path);
        }
        // 定时更新数据
        scheduleUploadData();
        // 定时检查节点
        scheduleCheckNode();
    }


    /**
     * 获取监听节点路径
     * @param monitor
     * @return
     */
    private String getMonitorPath(MonitorNode monitor) {
        if (MonitorNode.HOST_MODE_AUTO.equals(monitor.getMonitorHost())) {
            return monitor.getMonitorPath().concat("/").concat(NetUtil.getLocalhostStr() + ":" + environmentHelper.getPort());
        }
        return monitor.getMonitorPath().concat("/").concat(monitor.getMonitorHost());
    }


    /**
     * 监听节点事件
     * @param path
     */
    private void listenerNode(final String path) {
        CuratorCache cache = CuratorCache.build(client, path, CuratorCache.Options.SINGLE_NODE_CACHE);
        cache.start();
        // CuratorCacheListener
        cache.listenable().addListener((type, oldData, data) -> {
            switch (type) {
                case NODE_CREATED:
                    log.info("[{}]节点创建成功...........", path);
                    if (CollUtil.isNotEmpty(nodeEventListeners)) {
                        nodeEventListeners = nodeEventListeners.stream().sorted(Comparator.comparingInt(NodeEventListener::getSort)).collect(Collectors.toList());
                        for (NodeEventListener nodeEventListener : nodeEventListeners) {
                            nodeEventListener.nodeCreate(client, path, oldData, data);
                        }
                    }
                    break;
                case NODE_CHANGED:
                    log.info("[{}]节点数据发生改变, 老数据为: {}, 最新数据为: {}...........", path, oldData.toString(), data.toString());
                    if (CollUtil.isNotEmpty(nodeEventListeners)) {
                        nodeEventListeners = nodeEventListeners.stream().sorted(Comparator.comparingInt(NodeEventListener::getSort)).collect(Collectors.toList());
                        for (NodeEventListener nodeEventListener : nodeEventListeners) {
                            nodeEventListener.nodeChange(client, path, oldData, data);
                        }
                    }
                    break;
                case NODE_DELETED:
                    log.info("[{}]节点被删除...........", path);
                    if (CollUtil.isNotEmpty(nodeEventListeners)) {
                        nodeEventListeners = nodeEventListeners.stream().sorted(Comparator.comparingInt(NodeEventListener::getSort)).collect(Collectors.toList());
                        for (NodeEventListener nodeEventListener : nodeEventListeners) {
                            nodeEventListener.nodeDeleted(client, path, oldData, data);
                        }
                    }
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * 创建节点
     * @param path
     * @param monitor
     * @throws Exception
     */
    private void createNode(String path, MonitorNode monitor) throws Exception {
        String hostPath = NetUtil.getLocalhostStr() + ":" + environmentHelper.getPort();

        // 先把父节点创建出来，因为想要在父节点存储所有曾经创建过的子节点，这样能够方便的进行比较哪些节点被删除过
        Stat stat = client.checkExists().forPath(monitor.getMonitorPath());
        if (stat == null) {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(monitor.getMonitorPath());
        }

        // 处理父节点的数据
        String data = new String(client.getData().forPath(monitor.getMonitorPath()), StandardCharsets.UTF_8);
        if (StringUtils.isNotBlank(data)) {
            // 附加当前主机信息
            if (!data.contains(hostPath)) {
                data = data.concat(DATA_SPLIT_CHAR).concat(hostPath);
            }
        } else {
            data = hostPath;
        }
        client.setData().forPath(monitor.getMonitorPath(), data.getBytes(StandardCharsets.UTF_8));

        // 处理子节点的创建和数据
        // 创建子节点, 由于是临时节点，可以不用判断节点是否存在，如果不是临时节点，则需要判断
        if (monitor.isUseDefaultTimeStampUpload()) {
            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, String.valueOf(System.currentTimeMillis()).getBytes());
            }
            // 子节点存储时间戳数据
            client.setData().forPath(path, String.valueOf(System.currentTimeMillis()).getBytes());
        } else {
            if (client.checkExists().forPath(path) == null) {
                // 创建子节点
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            }
        }
    }

    /**
     * 更新节点内容
     */
    private void updateNode() {
        List<MonitorNode> monitors = monitorProperties.getMonitors();
        if (CollUtil.isEmpty(monitors)) {
            return;
        }

        String monitorPath;
        String data;
        for (MonitorNode monitor : monitors) {
            if (!monitor.isUseDefaultTimeStampUpload()) {
                continue;
            }
            monitorPath = getMonitorPath(monitor);
            try {
                data = String.valueOf(System.currentTimeMillis());
                log.info("【{}】上报数据{}", monitor, data);
                client.setData().forPath(monitorPath, data.getBytes(StandardCharsets.UTF_8));
            } catch (Exception exception) {
                // 节点被误删除, 重新建立节点
                if (exception instanceof KeeperException.NoNodeException) {
                    try {
                        log.info("更新节点[{}]时不存在， 重新建立节点", monitorPath);
                        createNode(monitorPath, monitor);
                    } catch (Exception e1) {
                        log.info("节点[{}]被误删除重建节点异常 {}", monitorPath, exception);
                    }
                } else {
                    log.error("节点[{}]更新失败", monitorPath, exception);
                }
            }
        }
    }

    /**
     * 定时更新节点数据
     */
    private void scheduleUploadData() {
        Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder.create().setNamePrefix("monitor-schedule-node-upload").build())
                .scheduleAtFixedRate(this::updateNode, 10, 10, TimeUnit.SECONDS);
    }


    /**
     * 定时检查节点是否存在, 由于当前的实现基本上是基于临时节点的， 对应服务下线后节点被删除，回调事件不一定能够被对应创建节点的主机处理，因为对应主机已经突然挂了，无法继续处理
     * 节点被删除事件回调
     *
     * 这里就需要依赖定时检查某个监听节点下的子节点，是否存在被删除的数据
     */
    private void scheduleCheckNode() {
        Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder.create().setNamePrefix("monitor-schedule-node-check").build())
                .scheduleAtFixedRate(() -> {
                    try {
                        checkNodeExist();
                    } catch (Exception exception) {
                        log.error("节点检查出现异常", exception);
                    }
                }, 6, 6, TimeUnit.SECONDS);
    }

    /**
     * 检查节点是否存在
     *
     */
    private void checkNodeExist() {
        zookeeperDistributedLock.lockWorkOnce(DistributedLock.formatPath(CHECK_NODE_LOCK_PATH), () -> {
            List<MonitorNode> monitors = monitorProperties.getMonitors();
            if (CollUtil.isEmpty(monitors)) {
                return;
            }
            ChildData childData;
            String monitorPath;
            for (MonitorNode monitor : monitors) {
                monitorPath = getMonitorPath(monitor);
                String data = new String(client.getData().forPath(monitor.getMonitorPath()), StandardCharsets.UTF_8);
                if (StringUtils.isBlank(data)) {
                    continue;
                }
                // 监听节点创建的所有节点历史记录
                String[] allNodes = data.split(DATA_SPLIT_CHAR);
                List<String> nodes = client.getChildren().forPath(monitor.getMonitorPath());
                if (CollUtil.isNotEmpty(nodeEventListeners)) {
                    if (CollUtil.isEmpty(nodes)) {
                        // 回调所有节点的监听事件
                        for (String currNode : allNodes) {
                            childData = new ChildData(monitor.getMonitorPath().concat("/").concat(currNode), client.checkExists().forPath(monitorPath), client.getData().forPath(monitorPath));
                            log.info("节点检查时发现[{}]被删除", childData.getPath());
                            if (CollUtil.isNotEmpty(nodeEventListeners)) {
                                nodeEventListeners = nodeEventListeners.stream().sorted(Comparator.comparingInt(NodeEventListener::getSort)).collect(Collectors.toList());
                                for (NodeEventListener nodeEventListener : nodeEventListeners) {
                                    nodeEventListener.nodeDeleted(client, childData.getPath(), childData, childData);
                                }
                            }
                        }
                        // 同步完成后就可以将父节点下的数据同步为当前节点列表了, 否则下次比较依然会触发删除事件
                        client.setData().forPath(monitor.getMonitorPath(), null);
                    } else {
                        Collection<String> disjunction = CollUtil.disjunction(Arrays.asList(allNodes), nodes);
                        if (CollUtil.isEmpty(disjunction)) {
                            return;
                        }
                        for (String currNode : disjunction) {
                            childData = new ChildData(monitor.getMonitorPath().concat("/").concat(currNode), client.checkExists().forPath(monitorPath), client.getData().forPath(monitorPath));
                            log.info("节点检查时发现[{}]被删除", childData.getPath());
                            if (CollUtil.isNotEmpty(nodeEventListeners)) {
                                nodeEventListeners = nodeEventListeners.stream().sorted(Comparator.comparingInt(NodeEventListener::getSort)).collect(Collectors.toList());
                                for (NodeEventListener nodeEventListener : nodeEventListeners) {
                                    nodeEventListener.nodeDeleted(client, childData.getPath(), childData, childData);
                                }
                            }
                        }
                        // 同步完成后就可以将父节点下的数据同步为当前节点列表了, 否则下次比较依然会触发删除事件
                        client.setData().forPath(monitor.getMonitorPath(), StringUtils.join(nodes, DATA_SPLIT_CHAR).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        });
    }
}
