# ddf-common-distributed-lock

分布式锁模块，提供基于 Redis 和 Zookeeper 的分布式锁实现。

## 功能特性

- Redis 分布式锁（Redisson）
- Zookeeper 分布式锁
- 锁续期机制
- 公平锁/非公平锁

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-distributed-lock</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径                    | 功能       |
|------------------------|----------|
| `DistributedLock`      | 分布式锁接口   |
| `RedisDistributedLock` | Redis 实现 |
| `ZkDistributedLock`    | ZK 实现    |

## 使用说明

```java
@Autowired
private DistributedLock distributedLock;

public void doWithLock(String lockKey, Runnable task) {
    distributedLock.executeWithLock(lockKey, 30, TimeUnit.SECONDS, task);
}
```
