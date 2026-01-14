# CLAUDE.md

## 模块简介

提供 ID 生成服务，支持雪花算法和号段模式两种方案。

## 核心类

| 类路径                                                            | 功能           |
|----------------------------------------------------------------|--------------|
| `com.ddf.common.ids.service.api.IdsApi`                        | ID 生成 API 接口 |
| `com.ddf.common.ids.service.impl.snowflake.SnowflakeIDGenImpl` | 雪花算法实现       |
| `com.ddf.common.ids.service.impl.segment.SegmentIDGenImpl`     | 号段模式实现       |
| `com.ddf.common.ids.service.config.properties.IdsProperties`   | 配置属性         |

## 使用说明

### 1. 雪花算法

```java
@Autowired
private IdsApi idsApi;

// 获取单个 ID
String id = idsApi.getSnowflakeId();

// 批量获取 ID
List<String> ids = idsApi.getSnowflakeIds(100);
```

### 2. 号段模式

```java
// 获取单个 ID
String id = idsApi.getSegmentId("order");

// 批量获取 ID
List<String> ids = idsApi.getSegmentIds("order", 100);
```

### 3. 自定义业务码

```java
// 使用业务码获取 ID
String id = idsApi.getSegmentId(BizCode.ORDER);
```

## 配置说明

### 雪花算法配置

```yaml
ddf:
  ids:
    begin-timestamp: 1704067200000   # 起始时间戳（毫秒）
    zk-address: localhost:2181       # ZK 地址
    port: 2088                       # 服务端口
```

### 号段模式配置

```yaml
ddf:
  ids:
    db-url: jdbc:mysql://localhost:3306/ids   # 数据库地址
    db-username: root
    db-password: password
    step: 1000                      # 每次获取的 ID 数量
```

## 算法对比

| 特性   | 雪花算法      | 号段模式 |
|------|-----------|------|
| 有序性  | 有序        | 有序   |
| 依赖   | Zookeeper | 数据库  |
| 性能   | 高         | 高    |
| 适用场景 | 高并发、分布式   | 中等并发 |

## 雪花算法原理

```
| 1 位符号位 | 41 位时间戳 | 5 位数据中心 | 5 位工作机器 | 12 位序列号 |
|    0     |   41 位     |    5 位     |    5 位     |   12 位    |
```

- 时间戳：约 69 年
- 数据中心：32 个
- 工作机器：32 个/数据中心
- 序列号：每毫秒 4096 个

## 注意事项

1. **ZK 配置**：雪花算法依赖 Zookeeper，必须配置正确的地址
2. **时钟回拨**：如果检测到时钟回拨超过 5ms，会抛出异常
3. **数据库连接**：号段模式需要配置数据库连接，确保数据库可用
4. **ID 浪费**：号段模式每次获取 `step` 个 ID，应用重启可能导致 ID 浪费
