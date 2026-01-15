# ddf-common-redis

Redis 集成模块，基于 Redisson 提供丰富的 Redis 操作。

## 功能特性

- Redisson 客户端集成
- 分布式锁
- 布隆过滤器
- 限流器

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-redis</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `RedissonClient` | Redis 客户端 |
| `RedisProperties` | 配置属性 |
| `RedisService` | Redis 服务 |

## 使用说明

### 配置

```yaml
ddf:
  redis:
    host: localhost
    port: 6379
    password: xxx
    database: 0
```

### 使用

```java
@Autowired
private RedisService redisService;

public void set(String key, Object value) {
    redisService.set(key, value, 3600);
}
```
