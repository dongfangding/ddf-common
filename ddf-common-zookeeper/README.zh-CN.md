# ddf-common-zookeeper

> Zookeeper 工具与节点监听支持模块。提供 Curator 客户端自动配置、节点事件监听、服务注册与配置管理能力，
> 可作为分布式锁、分布式 ID 等上层能力的底层支撑。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-zookeeper` 解决的是 **"分布式协调与状态同步"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| --- | --- | --- |
| 配置中心 | 多实例配置变更需逐个重启 | ZK 节点监听，配置热更新 |
| 服务注册发现 | 微服务上下线无法自动感知 | 临时节点注册，自动下线 |
| 分布式协调 | 选主、计数器、队列需求 | Curator Recipes（LeaderLatch、DistributedAtomicLong 等）|
| 分布式锁底层 | 需要强一致性锁 | 基于 ZK 的 `InterProcessMutex` |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-zookeeper</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  zookeeper:
    server-address: localhost:2181
    session-timeout: 60000
    connection-timeout: 15000
    namespace: /ddf
```

---

## 4. 核心 API

### 4.1 自动注入 CuratorFramework

引入依赖并配置后，`CuratorFramework` Bean 自动可用：

```java
@Autowired
private CuratorFramework curatorFramework;

public void createNode(String path, String data) throws Exception {
    curatorFramework.create()
        .creatingParentsIfNeeded()
        .forPath(path, data.getBytes(StandardCharsets.UTF_8));
}
```

### 4.2 节点监听

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
            // 处理配置变更
        });
        nodeCache.start();
    }
}
```

### 4.3 服务注册

```java
public void registerService(String serviceName, String address) throws Exception {
    String path = "/ddf/services/" + serviceName + "/" + address;
    if (curatorFramework.checkExists().forPath(path) == null) {
        curatorFramework.create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.EPHEMERAL)
            .forPath(path, address.getBytes());
    }
}
```

### 4.4 分布式锁（直接使用）

```java
InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/ddf/locks/order");
if (lock.acquire(10, TimeUnit.SECONDS)) {
    try {
        // 执行业务逻辑
    } finally {
        lock.release();
    }
}
```

> 更推荐使用 `ddf-common-distributed-lock` 模块的 `DistributedLock` 接口，它封装了 ZK 和 Redis 两种实现，API 更统一。

---

## 5. 进阶用法 / 扩展点

### 5.1 Leader 选举

```java
LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, "/ddf/leader", "instance-1");
leaderLatch.start();

if (leaderLatch.hasLeadership()) {
    // 当前实例为 Leader，执行定时任务等
}
```

### 5.2 分布式计数器

```java
DistributedAtomicLong counter = new DistributedAtomicLong(
    curatorFramework, "/ddf/counter/seq",
    new RetryNTimes(3, 100)
);
AtomicValue<Long> value = counter.increment();
```

### 5.3 配置分层管理

建议按以下结构组织 ZK 数据：

```
/ddf
├── config          # 配置中心
│   ├── app
│   ├── db
│   └── redis
├── services        # 服务注册
│   ├── service-a
│   └── service-b
└── locks           # 分布式锁
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| --- | --- |
| `ddf-common-distributed-lock` | ZK 分布式锁的上层封装，推荐业务使用 `DistributedLock` 接口 |
| `ddf-common-ids-service` | 分布式 ID 可能依赖 ZK 作为号段分配协调器 |
| `ddf-common-core` | JSON 序列化、字符串工具等基础支撑 |

---

## 7. FAQ

**Q1：Curator 与 ZooKeeper 版本兼容性？**
模块使用 Curator 5.3.0，要求 ZooKeeper 服务端 3.4.x 及以上。

**Q2：应用关闭时需要手动释放资源吗？**
`CuratorFramework` Bean 由 Spring 管理销毁，通常无需手动关闭。但若自行创建了 `NodeCache`、`LeaderLatch` 等，建议在 `@PreDestroy` 中关闭。

**Q3：ZK 连接断开会怎样？**
Curator 内置重连机制。`session-timeout` 内恢复连接，临时节点不会丢失；超时后临时节点自动删除，监听的服务下线。

**Q4：生产环境建议的 ZK 集群规模？**
建议 3 节点或 5 节点奇数部署，保证选举正常且有一定容错能力。

---

## 8. 参考

- 源码：`ZookeeperAutoConfiguration`、`NodeEventListener`
- Curator 文档：https://curator.apache.org/
- ZooKeeper 文档：https://zookeeper.apache.org/doc/current/
