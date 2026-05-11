# ddf-common-zookeeper

> Zookeeper utility and node listener support module. Provides Curator client auto-configuration, node event listening,
> service registration, and configuration management. Serves as the underlying support for higher-level capabilities such as distributed locks and distributed IDs.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-zookeeper` solves the **"distributed coordination and state synchronization"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Configuration center | Config changes require restarting every instance | ZK node watching for hot config reloads |
| Service registry/discovery | Microservice up/down events are not auto-detected | Ephemeral node registration with automatic deregistration |
| Distributed coordination | Leader election, counters, queues needed | Curator Recipes (LeaderLatch, DistributedAtomicLong, etc.) |
| Distributed lock primitive | Need strongly consistent locks | ZK-based `InterProcessMutex` |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-zookeeper</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
ddf:
  zookeeper:
    server-address: localhost:2181
    session-timeout: 60000
    connection-timeout: 15000
    namespace: /ddf
```

---

## 4. Core API

### 4.1 Auto-injected CuratorFramework

After importing the dependency and configuring it, the `CuratorFramework` bean is available automatically:

```java
@Autowired
private CuratorFramework curatorFramework;

public void createNode(String path, String data) throws Exception {
    curatorFramework.create()
        .creatingParentsIfNeeded()
        .forPath(path, data.getBytes(StandardCharsets.UTF_8));
}
```

### 4.2 Node watching

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
            // Handle config change
        });
        nodeCache.start();
    }
}
```

### 4.3 Service registration

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

### 4.4 Distributed lock (direct usage)

```java
InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/ddf/locks/order");
if (lock.acquire(10, TimeUnit.SECONDS)) {
    try {
        // Execute business logic
    } finally {
        lock.release();
    }
}
```

> It is recommended to use the `DistributedLock` interface from `ddf-common-distributed-lock`, which unifies ZK and Redis implementations under one API.

---

## 5. Advanced Usage / Extension Points

### 5.1 Leader election

```java
LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, "/ddf/leader", "instance-1");
leaderLatch.start();

if (leaderLatch.hasLeadership()) {
    // Current instance is leader; run scheduled jobs, etc.
}
```

### 5.2 Distributed counter

```java
DistributedAtomicLong counter = new DistributedAtomicLong(
    curatorFramework, "/ddf/counter/seq",
    new RetryNTimes(3, 100)
);
AtomicValue<Long> value = counter.increment();
```

### 5.3 Hierarchical config management

Recommended ZK data layout:

```
/ddf
├── config          # Configuration center
│   ├── app
│   ├── db
│   └── redis
├── services        # Service registry
│   ├── service-a
│   └── service-b
└── locks           # Distributed locks
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-distributed-lock` | Higher-level wrapper for ZK distributed locks; business code should prefer the `DistributedLock` interface |
| `ddf-common-ids-service` | Distributed IDs may use ZK as a segment allocator coordinator |
| `ddf-common-core` | JSON serialization, string utilities, and other fundamentals |

---

## 7. FAQ

**Q1: Curator and ZooKeeper version compatibility?**
This module uses Curator 5.3.0, which requires ZooKeeper server 3.4.x or higher.

**Q2: Do I need to manually release resources on application shutdown?**
The `CuratorFramework` bean is managed by Spring's destruction lifecycle and usually needs no manual cleanup. However, if you create `NodeCache`, `LeaderLatch`, etc. yourself, close them in `@PreDestroy`.

**Q3: What happens when the ZK connection drops?**
Curator has built-in reconnection. If the connection recovers within `session-timeout`, ephemeral nodes are preserved; after timeout they are automatically deleted, causing registered services to deregister.

**Q4: Recommended ZK cluster size for production?**
A 3-node or 5-node odd-numbered deployment is recommended to ensure proper election and provide fault tolerance.

---

## 8. References

- Source: `ZookeeperAutoConfiguration`, `NodeEventListener`
- Curator docs: https://curator.apache.org/
- ZooKeeper docs: https://zookeeper.apache.org/doc/current/
