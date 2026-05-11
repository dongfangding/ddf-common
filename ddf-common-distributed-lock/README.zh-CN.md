# ddf-common-distributed-lock

> 分布式锁模块。提供基于 Redis（Redisson）和 Zookeeper（Curator）的双实现，支持阻塞/非阻塞加锁、看门狗自动续期、失败回调等语义。
> 业务工程按需直接依赖本模块，也可通过 `ddf-common-starter-default` 间接引入。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-distributed-lock` 解决的是 **"分布式环境下的互斥执行"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| ----- | ----- | ----- |
| 库存扣减 | 高并发下同一商品超卖 | `tryLock` 非阻塞抢锁，失败即返回 |
| 订单幂等 | 重复请求导致重复下单 | `lockWork` 阻塞直到获取锁，保证串行执行 |
| 定时任务集群 | 多实例同时触发同一任务 | 看门狗续期，防止任务执行期间锁过期 |
| 数据对账 | 需要较长持有时间的批处理 | `lockWork(key, leaseTime, ...)` 显式指定租约 |
| 高可靠锁 | 对锁的可靠性要求高于性能 | Zookeeper 实现，利用临时顺序节点 |

---

## 2. 依赖引入

直接依赖本模块（不在 `starter-web` 中，需显式引入）：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-distributed-lock</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

或通过默认 starter：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

实现层面：
- **Redis 锁**：依赖 `redisson`（由 `ddf-common-redis` 引入 `RedissonClient`）
- **ZK 锁**：依赖 `curator-recipes`，模块自带 Curator 客户端初始化

---

## 3. 最小化配置

### 3.1 Redis 分布式锁

```yaml
customizer:
  infra:
    distributed:
      lock:
        redis:
          enable: true
```

> 前提：上下文中已存在 `RedissonClient` Bean（如引入 `ddf-common-redis` 并配置 Redis 连接）。

### 3.2 Zookeeper 分布式锁

```yaml
customizer:
  infra:
    distributed:
      lock:
        zookeeper:
          enable: true
          connect-string: "127.0.0.1:2181"   # ZK 地址
          root: "/distributed_lock"          # 根路径
          session-timeout-ms: 60000          # Session 超时
          connection-timeout-ms: 10000       # 连接超时
          retry-count: 3                     # 重试次数
          elapsed-time-ms: 2000              # 重试间隔
```

两种实现**互不影响**，可按需只启用其一，或同时启用并通过 Bean 名称区分注入。

---

## 4. 核心 API 使用指南

### 4.1 接口定义

```java
public interface DistributedLock {
    // 非阻塞尝试获取锁（最多等待 waitTime）
    <R> R tryLock(String lockKey, int waitTime, TimeUnit timeUnit,
                  BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler);

    // 只尝试一次，获取不到立刻失败
    default <R> R tryLockOnce(String lockKey, BusinessHandler<R> successHandler,
                              BusinessHandler<R> failureHandler);

    // 阻塞获取锁 + 显式租约（看门狗失效）
    <R> R lockWork(String lockKey, int leaseTime, TimeUnit timeUnit,
                   BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler);

    // 阻塞获取锁 + 看门狗自动续期
    <R> R lockWork(String lockKey, BusinessHandler<R> successHandler,
                   BusinessHandler<R> failureHandler);
}
```

### 4.2 Redis 锁

#### 非阻塞尝试（常用于库存扣减）

```java
@Autowired
@Qualifier("redisDistributedLock")
private DistributedLock distributedLock;

public ResponseData<Void> deductStock(Long productId, Integer count) {
    String lockKey = "lock:stock:" + productId;
    return distributedLock.tryLock(
        lockKey,
        3, TimeUnit.SECONDS,           // 最多等 3 秒
        () -> {                        // successHandler
            stockService.deduct(productId, count);
            return ResponseData.success();
        },
        () -> {                        // failureHandler
            return ResponseData.failure(BaseErrorCallbackCode.REQUEST_TOO_MANY);
        }
    );
}
```

#### 阻塞 + 看门狗续期（常用于订单处理）

```java
public OrderVO createOrder(CreateOrderRequest request) {
    String lockKey = "lock:order:" + request.getOrderNo();
    return distributedLock.lockWork(
        lockKey,
        () -> orderService.create(request),   // 看门狗自动续期，业务执行多久锁就续多久
        null                                  // 阻塞模式失败回调基本不会触发
    );
}
```

#### 只尝试一次（常用于防重兜底）

```java
public ResponseData<Void> idempotentProcess(String bizNo) {
    return distributedLock.tryLockOnce(
        "lock:biz:" + bizNo,
        () -> {
            processor.process(bizNo);
            return ResponseData.success();
        },
        () -> ResponseData.failure(BaseErrorCallbackCode.REQUEST_TOO_MANY)
    );
}
```

#### 显式租约（定时任务场景）

```java
// 最长持有 60 秒，超过后无论业务是否完成都会强制释放
public void reconciliation() {
    distributedLock.lockWork(
        "lock:reconciliation:daily",
        60, TimeUnit.SECONDS,
        () -> { reconciliationService.run(); return null; },
        null
    );
}
```

### 4.3 Zookeeper 锁

```java
@Autowired
@Qualifier("zookeeperDistributedLock")
private DistributedLock zkLock;

public ResponseData<Void> criticalSection(String resourceId) {
    // ZK 锁的 key 必须以 / 开头
    return zkLock.lockWork(
        "/resource/" + resourceId,
        () -> {
            resourceService.process(resourceId);
            return ResponseData.success();
        },
        null
    );
}
```

ZK 锁的 `formatLockKey` 会自动拼接为：`{root}/{env}/locks{lockKey}`。
例如配置 `root=/distributed_lock`、`env=prod`、`lockKey=/resource/1001`，
实际节点路径为 `/distributed_lock/prod/locks/resource/1001`。

### 4.4 两种实现对比

| 特性 | Redis 锁（Redisson） | ZK 锁（Curator） |
| ----- | ----- | ----- |
| 实现 | `RLock` | `InterProcessMutex` |
| 可重入 | 支持 | 支持 |
| 看门狗续期 | 支持（不指定 leaseTime 时） | 不支持 |
| 阻塞模式 | `lock.lock()` 无限阻塞 | `acquire()` 可指定等待时间 |
| 异常释放 | 应用崩溃后 Redis key 过期自动释放 | 会话断开临时节点自动删除 |
| 适用场景 | 高并发、性能敏感 | 可靠性要求极高 |
| Key 格式 | `:` 分隔的字符串 | 必须以 `/` 开头的路径 |

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义锁实现

实现 `DistributedLock` 接口并注册为 Bean：

```java
@Component("mySqlDistributedLock")
public class MySqlDistributedLock implements DistributedLock {
    @Override
    public <R> R tryLock(String lockKey, int waitTime, TimeUnit timeUnit,
            BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler) {
        // 基于数据库唯一索引或乐观锁实现
        // ...
    }
    // ... 其它方法
}
```

### 5.2 统一封装业务模板

避免在业务代码中散落锁 Key 和回调：

```java
@Component
public class OrderLockTemplate {

    @Autowired
    @Qualifier("redisDistributedLock")
    private DistributedLock lock;

    public <R> R executeWithOrderLock(Long orderId, DistributedLock.BusinessHandler<R> handler) {
        return lock.lockWork("lock:order:" + orderId, handler, null);
    }
}
```

### 5.3 多实现并存时的注入

当同时启用 Redis 和 ZK 锁时，通过 `@Qualifier` 明确指定：

```java
@Autowired
@Qualifier("redisDistributedLock")
private DistributedLock redisLock;

@Autowired
@Qualifier("zookeeperDistributedLock")
private DistributedLock zkLock;
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| ----- | ----- |
| `ddf-common-redis` | Redis 锁依赖 `RedissonClient` Bean，通常由 `ddf-common-redis` 提供 |
| `ddf-common-zookeeper` | ZK 锁依赖 Curator 客户端；若已引入 `ddf-common-zookeeper` 可复用其 `CuratorFramework` |
| `ddf-common-api` | 异常体系 `LockingAcquireException`、`LockingBusinessException`、`LockingReleaseException` |
| `ddf-common-starter-default` | 默认 starter 已包含本模块，业务无需额外声明依赖 |

---

## 7. FAQ

**Q1：Redis 锁的看门狗续期机制是什么？**  
当调用 `lockWork(key, success, failure)`（不指定 leaseTime）时，Redisson 默认启用看门狗线程，每 10 秒检查一次并续期锁的过期时间。只要持有锁的 JVM 进程存活，锁就不会过期。业务执行完毕后需调用 `unlock()` 释放。

**Q2：为什么 `lockWork` 的 failureHandler 在 Redis 中很少触发？**  
Redis 的 `lockWork` 使用 `lock.lock()` 是**无限阻塞**模式，除非加锁过程中抛出异常（如 Redisson 连接断开），否则必然能拿到锁。因此 `failureHandler` 主要服务于 ZK 实现或异常兜底。

**Q3：ZK 锁的 key 为什么必须以 `/` 开头？**  
Zookeeper 的节点路径是类 Unix 文件系统风格，必须以 `/` 开头。模块会在 `formatLockKey` 中自动拼接 `root`、`env` 和 `/locks` 前缀，但业务传入的 `lockKey` 自身也需符合路径规范。

**Q4：锁释放失败会怎样？**  
两种实现均在 `finally` 块中尝试释放锁，释放失败仅记录 error 日志，不会抛异常干扰业务。Redis 锁还会校验 `isHeldByCurrentThread()`，防止误释放其他线程持有的锁。

**Q5：能否在一个事务中使用分布式锁？**  
可以，但要注意锁的释放时机应晚于事务提交。如果锁在事务提交前释放，可能出现另一个线程在事务尚未 flush 时获取锁并读到脏数据。推荐将锁范围包裹在事务外层，或使用事务回调确保释放顺序。

**Q6：ZK 锁没有看门狗，业务执行时间很长怎么办？**  
Curator 的 `InterProcessMutex` 锁与 ZK Session 绑定，Session 存活期间锁一直有效。可通过增大 `session-timeout-ms` 来延长锁的生存期，但不建议让单任务持有锁过久——应将长任务拆分为短事务 + 分布式锁的细粒度控制。

---

## 8. 参考

- 源码：`DistributedLock.java`
- 源码：`redis/impl/RedisDistributedLock.java`、`redis/config/RedisLockConfiguration.java`、`redis/config/DistributedLockRedisProperties.java`
- 源码：`zk/impl/ZookeeperDistributedLock.java`、`zk/config/CuratorFrameworkConfig.java`、`zk/config/DistributedLockZookeeperProperties.java`
- 源码：`config/DistributedLockAutoConfiguration.java`
- 源码：`exception/LockingAcquireException.java`、`exception/LockingBusinessException.java`、`exception/LockingReleaseException.java`
