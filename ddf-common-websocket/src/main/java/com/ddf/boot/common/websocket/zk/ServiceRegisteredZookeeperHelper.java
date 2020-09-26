package com.ddf.boot.common.websocket.zk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 提供一个注册节点，将当前websocket服务注册到zk的节点中， 一旦当前服务掉线，由zk感知，让其它机器感知机器下线后，将对应机器上的所有设备下线掉
 * <p>
 * 但是要注意控制， 这里只是提供监控。如果客户端有重连机制的话，要注意下线的时候只下线这台机器上的数据， 如果更新要有对应服务器主机这个条件；
 * <p>
 * 因为重连的话，客户端会连接到其它服务器上，不能下线已经重连到其它服务器的客户端
 *
 * @author dongfang.ding
 **/
@Slf4j
public class ServiceRegisteredZookeeperHelper {
    /**
     * 本机ip
     */
    private final String ip;
    /**
     * 当前服务端口
     */
    private final String port;
    /**
     * 监听地址数据， ip:port
     */
    private final String listenAddress;
    /**
     * 当前监听程序创建路径前缀
     */
    private static final String PREFIX_ZK_PATH = "/message-ws/com.company.pay";
    /**
     * 最终连接信息节点
     */
    public static final String PATH_MONITOR = PREFIX_ZK_PATH + "/monitor";
    /**
     * 本机zk znode
     */
    private String zkAddressNode = null;

    private long lastUpdateTime;

    public ServiceRegisteredZookeeperHelper(String ip, String port) {
        this.ip = ip;
        this.port = port;
        this.listenAddress = ip + ":" + port;
    }

    public boolean init(CuratorFramework curator) {
        try {
            Stat stat = curator.checkExists().forPath(PATH_MONITOR);
            if (stat == null) {
                zkAddressNode = createNode(curator);
                return true;
            } else {
                // 存在最终创建的监听节点,先检查是否有属于自己的根节点
                List<String> keys = curator.getChildren().forPath(PATH_MONITOR);
                if (!CollectionUtils.isEmpty(keys) && keys.contains(listenAddress)) {
                    // zkAddressNode = listenAddress;
                    // if (!checkInitTimeStamp(curator, zkAddressNode))
                    //    throw new CheckLastTimeException("init timestamp check error,forever node timestamp gt this node time");
                    // ScheduledUploadData(curator, zkAddressNode);
                } else {
                    //表示新启动的节点,创建持久节点
                    String newNode = createNode(curator);
                    // zkAddressNode = newNode;
                    // ScheduledUploadData(curator, zkAddressNode);
                }
            }
        } catch (Exception e) {
            log.info("service registered fail, crate node error {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 创建持久顺序节点 ,并把节点数据放入 value
     *
     * @param curator
     * @return
     * @throws Exception
     */
    private String createNode(CuratorFramework curator) throws Exception {
        try {
            return curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(PATH_MONITOR + "/" + listenAddress, buildData().getBytes());
        } catch (Exception e) {
            log.error("create node error msg {} ", e.getMessage());
            throw e;
        }
    }

    /**
     * 构建需要上传的数据
     *
     * @return
     */
    private String buildData() throws JsonProcessingException {
        Endpoint endpoint = new Endpoint(ip, port, System.currentTimeMillis());
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(endpoint);
        return json;
    }

    private Endpoint deBuildData(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Endpoint endpoint = mapper.readValue(json, Endpoint.class);
        return endpoint;
    }

    private void ScheduledUploadData(final CuratorFramework curator, final String zkAddressNode) {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "message-ws-schedule-upload-time");
            thread.setDaemon(true);
            return thread;
        }).scheduleWithFixedDelay(() -> updateNewData(curator, zkAddressNode), 1L, 3L, TimeUnit.SECONDS);//每3s上报数据

    }

    private void updateNewData(CuratorFramework curator, String path) {
        try {
            if (System.currentTimeMillis() < lastUpdateTime) {
                return;
            }
            curator.setData().forPath(path, buildData().getBytes());
            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            if (e instanceof KeeperException.NoNodeException) {
                // 节点被误删除
                try {
                    createNode(curator);
                } catch (Exception e1) {
                    log.info("节点被误删除重建节点异常 {}", e1.getMessage());
                }
            }
            log.info("update init data error path is {} error is {}", path, e);
        }
    }

    private boolean checkInitTimeStamp(CuratorFramework curator, String zk_AddressNode) throws Exception {
        byte[] bytes = curator.getData().forPath(zk_AddressNode);
        Endpoint endPoint = deBuildData(new String(bytes));
        //该节点的时间不能小于最后一次上报的时间
        return !(endPoint.getTimestamp() > System.currentTimeMillis());
    }

    /**
     * 上报数据结构
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Endpoint {
        private String ip;
        private String port;
        private long timestamp;
    }
}
