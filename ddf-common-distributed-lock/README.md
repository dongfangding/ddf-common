# ddf-common-distributed-lock

> Distributed locking module. Provides dual implementations based on Redis (Redisson) and Zookeeper (Curator),
> supporting blocking / non-blocking acquisition, watchdog auto-renewal, and failure callbacks.
> Application services depend on this module directly, or pull it in transitively through `ddf-common-starter-default`.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-distributed-lock` solves the **"mutual exclusion in a distributed environment"** problem.

| Category                  | Typical Problem                                           | What the Module Provides                                        |
|---------------------------|-----------------------------------------------------------|-----------------------------------------------------------------|
| Stock deduction           | High-concurrency overselling of the same product          | `tryLock` non-blocking acquisition; fail fast                   |
| Order idempotency         | Duplicate requests creating duplicate orders              | `lockWork` blocks until acquired, guaranteeing serial execution |
| Clustered scheduled tasks | Multiple instances triggering the same job simultaneously | Watchdog renewal prevents lock expiration during long tasks     |
| Data reconciliation       | Batch processing requiring long hold times                | `lockWork(key, leaseTime, ...)` with explicit lease duration    |
| High-reliability locking  | Reliability requirements outweigh performance             | Zookeeper implementation using ephemeral sequential nodes       |

---

## 2. Maven Dependency

Depend on this module directly (not included in `starter-web`; must be explicit):

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-distributed-lock</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Or through the default starter:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Implementation requirements:

- **Redis lock**: depends on `redisson` (`RedissonClient` bean provided by `ddf-common-redis`)
- **ZK lock**: depends on `curator-recipes`; the module initializes its own Curator client

---

## 3. Minimum Configuration

### 3.1 Redis distributed lock

```yaml
customizer:
  infra:
    distributed:
      lock:
        redis:
          enable: true
```

> Prerequisite: a `RedissonClient` bean exists in the context (e.g. from `ddf-common-redis` with Redis connection configured).

### 3.2 Zookeeper distributed lock

```yaml
customizer:
  infra:
    distributed:
      lock:
        zookeeper:
          enable: true
          connect-string: "127.0.0.1:2181"   # ZK address
          root: "/distributed_lock"          # Root path
          session-timeout-ms: 60000          # Session timeout
          connection-timeout-ms: 10000       # Connection timeout
          retry-count: 3                     # Retry count
          elapsed-time-ms: 2000              # Retry interval
```

The two implementations are **independent**; enable either or both, and distinguish by bean name when injecting.

---

## 4. Core API Guide

### 4.1 Interface definition

```java
public interface DistributedLock {
    // Non-blocking attempt (waits at most waitTime)
    <R> R tryLock(String lockKey, int waitTime, TimeUnit timeUnit,
                  BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler);

    // Try once only; fail immediately if not acquired
    default <R> R tryLockOnce(String lockKey, BusinessHandler<R> successHandler,
                              BusinessHandler<R> failureHandler);

    // Blocking acquisition + explicit lease (watchdog disabled)
    <R> R lockWork(String lockKey, int leaseTime, TimeUnit timeUnit,
                   BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler);

    // Blocking acquisition + watchdog auto-renewal
    <R> R lockWork(String lockKey, BusinessHandler<R> successHandler,
                   BusinessHandler<R> failureHandler);
}
```

### 4.2 Redis lock

#### Non-blocking attempt (typical for stock deduction)

```java
@Autowired
@Qualifier("redisDistributedLock")
private DistributedLock distributedLock;

public ResponseData<Void> deductStock(Long productId, Integer count) {
    String lockKey = "lock:stock:" + productId;
    return distributedLock.tryLock(
        lockKey,
        3, TimeUnit.SECONDS,           // Wait at most 3 seconds
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

#### Blocking + watchdog renewal (typical for order processing)

```java
public OrderVO createOrder(CreateOrderRequest request) {
    String lockKey = "lock:order:" + request.getOrderNo();
    return distributedLock.lockWork(
        lockKey,
        () -> orderService.create(request),   // Watchdog auto-renews for the duration of business execution
        null                                  // Failure callback rarely triggers in blocking mode
    );
}
```

#### Try once only (idempotency fallback)

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

#### Explicit lease (scheduled task scenario)

```java
// Held for at most 60 seconds; forcibly released afterwards regardless of completion
public void reconciliation() {
    distributedLock.lockWork(
        "lock:reconciliation:daily",
        60, TimeUnit.SECONDS,
        () -> { reconciliationService.run(); return null; },
        null
    );
}
```

### 4.3 Zookeeper lock

```java
@Autowired
@Qualifier("zookeeperDistributedLock")
private DistributedLock zkLock;

public ResponseData<Void> criticalSection(String resourceId) {
    // ZK lock keys must start with /
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

ZK `formatLockKey` automatically concatenates: `{root}/{env}/locks{lockKey}`.
For example, with `root=/distributed_lock`, `env=prod`, `lockKey=/resource/1001`,
the actual node path is `/distributed_lock/prod/locks/resource/1001`.

### 4.4 Implementation comparison

| Feature          | Redis lock (Redisson)                   | ZK lock (Curator)                            |
|------------------|-----------------------------------------|----------------------------------------------|
| Implementation   | `RLock`                                 | `InterProcessMutex`                          |
| Reentrant        | Yes                                     | Yes                                          |
| Watchdog renewal | Yes (when leaseTime is not specified)   | No                                           |
| Blocking mode    | `lock.lock()` blocks indefinitely       | `acquire()` with configurable wait time      |
| Failure release  | Redis key TTL expires after app crash   | Ephemeral node deleted on session disconnect |
| Use case         | High concurrency, performance sensitive | Extreme reliability requirements             |
| Key format       | `:`-delimited string                    | Path starting with `/`                       |

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom lock implementation

Implement `DistributedLock` and register as a bean:

```java
@Component("mySqlDistributedLock")
public class MySqlDistributedLock implements DistributedLock {
    @Override
    public <R> R tryLock(String lockKey, int waitTime, TimeUnit timeUnit,
            BusinessHandler<R> successHandler, BusinessHandler<R> failureHandler) {
        // Implement with database unique index or optimistic locking
        // ...
    }
    // ... other methods
}
```

### 5.2 Unified business template

Avoid scattering lock keys and callbacks across business code:

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

### 5.3 Injecting when multiple implementations coexist

When both Redis and ZK locks are enabled, use `@Qualifier`:

```java
@Autowired
@Qualifier("redisDistributedLock")
private DistributedLock redisLock;

@Autowired
@Qualifier("zookeeperDistributedLock")
private DistributedLock zkLock;
```

---

## 6. Interplay with Other Modules

| Module                       | How They Cooperate                                                                                            |
|------------------------------|---------------------------------------------------------------------------------------------------------------|
| `ddf-common-redis`           | Redis lock depends on the `RedissonClient` bean, usually provided by `ddf-common-redis`                       |
| `ddf-common-zookeeper`       | ZK lock depends on Curator client; if `ddf-common-zookeeper` is present, its `CuratorFramework` can be reused |
| `ddf-common-api`             | Exception taxonomy: `LockingAcquireException`, `LockingBusinessException`, `LockingReleaseException`          |
| `ddf-common-starter-default` | This module is included in the default starter; no extra dependency needed                                    |

---

## 7. FAQ

**Q1: What is the Redis watchdog renewal mechanism?**  
When calling `lockWork(key, success, failure)` without specifying `leaseTime`, Redisson enables a watchdog thread that checks every 10 seconds and renews the lock expiration. As long as the JVM process holding the lock is alive, the lock never expires. `unlock()` must be called after business completion.

**Q2: Why is the `failureHandler` in `lockWork` rarely triggered for Redis?**  
Redis `lockWork` uses `lock.lock()`, which **blocks indefinitely** until the lock is acquired. Unless an exception is thrown during locking (e.g. Redisson connection failure), the lock is guaranteed to be obtained. Therefore `failureHandler` mainly serves the ZK implementation or acts as an exception fallback.

**Q3: Why must ZK lock keys start with `/`?**  
Zookeeper node paths follow a Unix-like filesystem style and must start with `/`. The module automatically concatenates `root`, `env`, and `/locks` prefixes in `formatLockKey`, but the business-provided `lockKey` itself must also conform to path conventions.

**Q4: What happens if lock release fails?**  
Both implementations attempt release in a `finally` block; failure is logged at error level without throwing exceptions that could interfere with business logic. The Redis lock also checks `isHeldByCurrentThread()` to prevent releasing a lock held by another thread.

**Q5: Can distributed locks be used inside a transaction?**  
Yes, but ensure the lock is released **after** the transaction commits. If the lock is released before commit, another thread might acquire the lock and read unflushed dirty data. Wrap the lock scope outside the transaction, or use transaction callbacks to guarantee release order.

**Q6: ZK locks have no watchdog; what if business execution takes a very long time?**  
Curator's `InterProcessMutex` is bound to the ZK Session; the lock remains valid while the Session is alive. Increase `session-timeout-ms` to extend lock lifetime, but avoid holding a lock for extended periods — break long tasks into shorter transactions with fine-grained distributed locking.

---

## 8. References

- Source: `DistributedLock.java`
- Source: `redis/impl/RedisDistributedLock.java`, `redis/config/RedisLockConfiguration.java`, `redis/config/DistributedLockRedisProperties.java`
- Source: `zk/impl/ZookeeperDistributedLock.java`, `zk/config/CuratorFrameworkConfig.java`, `zk/config/DistributedLockZookeeperProperties.java`
- Source: `config/DistributedLockAutoConfiguration.java`
- Source: `exception/LockingAcquireException.java`, `exception/LockingBusinessException.java`, `exception/LockingReleaseException.java`
