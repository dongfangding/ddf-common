# CLAUDE.md

## 模块简介

提供分布式锁功能，支持 Redis（Redisson）和 Zookeeper（Curator）两种实现。

## 核心类

| 类路径                                                                     | 功能           |
|-------------------------------------------------------------------------|--------------|
| `com.ddf.boot.common.lock.DistributedLock`                              | 分布式锁接口       |
| `com.ddf.boot.common.lock.redis.impl.RedisDistributedLock`              | Redis 分布式锁实现 |
| `com.ddf.boot.common.lock.zk.impl.ZookeeperDistributedLock`             | ZK 分布式锁实现    |
| `com.ddf.boot.common.lock.redis.config.DistributedLockRedisProperties`  | Redis 锁配置    |
| `com.ddf.boot.common.lock.zk.config.DistributedLockZookeeperProperties` | ZK 锁配置       |

## 使用说明

### 1. 注入分布式锁

```java
@Autowired
private DistributedLock distributedLock;
```

### 2. 阻塞获取锁（带看门狗）

```java
// 使用 Redis 锁
Result<T> result = distributedLock.lockWork(
    "lock:order:%s".formatted(orderId),  // 锁 key
    BusinessHandler.<T>builder()
        .successHandler(data -> {
            // 获取锁成功的业务逻辑
            return orderService.processOrder(data);
        })
        .failureHandler(() -> {
            // 获取锁失败的逻辑
            return null;
        })
        .build()
);
```

### 3. 尝试获取锁（带等待时间）

```java
// 尝试获取锁，最多等待 5 秒
Result<T> result = distributedLock.tryLock(
    "lock:stock:%s".formatted(productId),  // 锁 key
    5,                                      // 等待时间
    TimeUnit.SECONDS,                       // 时间单位
    BusinessHandler.<T>builder()
        .successHandler(() -> {
            return decreaseStock(productId, count);
        })
        .failureHandler(() -> {
            return Result.fail("获取锁失败");
        })
        .build()
);
```

### 4. 指定租约时间

```java
// 锁持有时间最长 30 秒
Result<T> result = distributedLock.lockWork(
    "lock:payment:%s".formatted(orderId),
    30,                  // 租约时间（秒）
    TimeUnit.SECONDS,
    BusinessHandler.<T>builder()
        .successHandler(this::processPayment)
        .build()
);
```

### 5. ZK 锁使用

```java
// ZK 锁路径必须以 / 开头
Result<T> result = zookeeperDistributedLock.lockWork(
    "/locks/order/%s".formatted(orderId),  // 注意：必须以 / 开头
    BusinessHandler.<T>builder()
        .successHandler(this::processOrder)
        .build()
);
```

## 配置说明

### Redis 锁配置

```yaml
ddf:
  distributed-lock:
    redis:
      lock-prefix: "ddf:lock:"    # 锁 key 前缀
```

### ZK 锁配置

```yaml
ddf:
  distributed-lock:
    zookeeper:
      root: "/ddf/locks"          # ZK 根路径
      env: "dev"                   # 环境标识
      lock-root: "/locks"          # 锁路径
```

## 对比

| 特性   | Redis 锁   | ZK 锁    |
|------|-----------|---------|
| 实现   | Redisson  | Curator |
| 可重入  | 支持        | 支持      |
| 公平锁  | 支持        | 支持      |
| 看门狗  | 支持（自动续期）  | 不支持     |
| 适用场景 | 高并发、性能要求高 | 可靠性要求极高 |

## 注意事项

1. **锁 Key 规范**：Redis 锁使用 `:` 分隔，ZK 锁必须以 `/` 开头
2. **租约时间**：使用看门狗机制时无需指定 `leaseTime`，应用崩溃后锁会自动释放
3. **异常处理**：锁释放失败会记录日志，不会抛出异常
4. **ZK 版本**：Curator 5.3.0 要求 ZooKeeper 3.4.x+
