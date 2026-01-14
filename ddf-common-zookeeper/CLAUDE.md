# CLAUDE.md

## 模块简介

提供 Zookeeper 服务发现、节点监听和配置管理功能。

## 核心类

| 类路径                                                           | 功能      |
|---------------------------------------------------------------|---------|
| `com.ddf.boot.zookeeper.ZookeeperAutoConfiguration`           | 自动配置    |
| `com.ddf.boot.zookeeper.listener.NodeEventListener`           | 节点事件监听器 |
| `com.ddf.boot.zookeeper.monitor.config.MonitorRegistryConfig` | 配置管理    |

## 使用说明

### 1. 配置

```yaml
ddf:
  zookeeper:
    server-address: localhost:2181   # ZK 服务器地址
    session-timeout: 60000           # 会话超时（毫秒）
    connection-timeout: 15000        # 连接超时（毫秒）
    namespace: /ddf                  # 命名空间
```

### 2. 监听节点变化

```java
@Component
public class ConfigWatcher {

    @Autowired
    private CuratorFramework curatorFramework;

    @PostConstruct
    public void watchConfig() throws Exception {
        String path = "/ddf/config/app";

        NodeCache nodeCache = new NodeCache(curatorFramework, path);
        nodeCache.getListenable().addListener(() -> {
            byte[] data = nodeCache.getCurrentData().getData();
            String config = new String(data);
            // 处理配置变更
        });
        nodeCache.start();
    }
}
```

### 3. 服务注册

```java
@Autowired
private CuratorFramework curatorFramework;

public void registerService(String serviceName, String address) {
    String path = "/ddf/services/" + serviceName + "/" + address;
    try {
        if (curatorFramework.checkExists().forPath(path) == null) {
            curatorFramework.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, address.getBytes());
        }
    } catch (Exception e) {
        log.error("服务注册失败", e);
    }
}
```

## ZK 数据结构

```
/ddf
├── config          # 配置中心
│   ├── app
│   ├── db
│   └── redis
├── services        # 服务注册
│   ├── service-a
│   │   ├── 192.168.1.1:8080
│   │   └── 192.168.1.2:8080
│   └── service-b
└── locks           # 分布式锁
```

## 注意事项

1. **Curator 版本**：5.3.0 要求 ZooKeeper 3.4.x+
2. **连接管理**：确保在应用关闭时正确关闭 `CuratorFramework`
3. **权限控制**：生产环境建议配置 ACL 权限
4. **会话超时**：根据网络状况调整超时时间
