# ddf-common-ids-service

> 分布式 ID 生成服务模块。支持雪花算法（Snowflake）和号段模式（Segment）两种方案，
> 为分布式系统提供高性能、高可用的全局唯一 ID。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-ids-service` 解决的是 **"分布式环境下如何生成全局唯一 ID"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| --- | --- | --- |
| 分库分表主键 | 自增 ID 在分片后冲突 | 雪花算法生成全局唯一趋势递增 ID |
| 订单号生成 | 需要短、有序、不重复的订单号 | 号段模式按业务码批量分配 |
| 高并发写入 | 数据库自增成为瓶颈 | 本地缓存号段，减少 DB 访问 |
| 数据迁移 | 多数据中心合并后 ID 冲突 | 雪花算法通过数据中心位隔离 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ids-service</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

### 雪花算法模式

```yaml
customizer:
  infra:
    ids:
      snowflakeEnable: true
      beginTimestamp: 1609430400000   # 起始时间戳（毫秒）
      zkAddress: 127.0.0.1:2181       # Zookeeper 地址
      port: 2181
```

### 号段模式

```yaml
customizer:
  infra:
    ids:
      segmentEnable: true
      # 号段模式依赖数据源，需配置数据库连接
```

---

## 4. 核心 API

### 4.1 雪花算法 ID

```java
@Autowired
private IdsApi idsApi;

// 获取单个 ID
String id = idsApi.getSnowflakeId();   // e.g. 1785643298765432123

// 批量获取 ID
List<String> ids = idsApi.getSnowflakeIds(100);
```

### 4.2 号段模式 ID

```java
// 按业务类型获取单个 ID
String orderId = idsApi.getSegmentId("order");

// 批量获取
List<String> orderIds = idsApi.getSegmentIds("order", 100);

// 使用业务码枚举
String id = idsApi.getSegmentId(BizCode.ORDER);
```

---

## 5. 进阶用法 / 扩展点

### 5.1 算法选型对比

| 特性 | 雪花算法 | 号段模式 |
| --- | --- | --- |
| 依赖 | Zookeeper | 数据库 |
| 性能 | 极高（本地生成） | 高（本地缓存号段） |
| 有序性 | 趋势递增 | 严格递增 |
| 适用场景 | 高并发、分布式 | 中等并发、需要严格连续 |
| 时钟回拨 | 敏感（需处理） | 不敏感 |

### 5.2 雪花算法结构

```
| 1 位符号位 | 41 位时间戳 | 5 位数据中心 | 5 位工作机器 | 12 位序列号 |
```

- 时间戳范围：约 69 年
- 数据中心：32 个
- 工作机器：32 个/数据中心
- 序列号：每毫秒 4096 个

### 5.3 自定义号段表

号段模式可通过自定义 `IDAllocDao` 接入自己的号段分配表：

```java
@Component
public class CustomIDAllocDao implements IDAllocDao {
    @Override
    public SegmentBuffer getBuffer(String bizTag) {
        // 从自定义表查询号段
    }
}
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| --- | --- |
| `ddf-common-zookeeper` | 雪花算法依赖 ZK 分配工作机器 ID |
| `ddf-common-data-mysql-starter` | 号段模式依赖数据库存储号段 |
| `ddf-common-core` | 时间工具、并发工具等基础支撑 |

---

## 7. FAQ

**Q1：雪花算法时钟回拨怎么处理？**
若检测到时钟回拨超过 5ms，模块会抛出异常。建议配合 NTP 服务并配置告警。

**Q2：号段模式的 ID 会浪费吗？**
可能。每次从数据库获取 `step` 个 ID 缓存在本地，应用重启时未用完的号段会丢失。

**Q3：两种模式可以同时启用吗？**
可以。业务根据场景选择调用 `getSnowflakeId()` 或 `getSegmentId()`。

**Q4：模块实现参考了哪些开源方案？**
参考了美团 Leaf 的设计思路，并在当前项目中做了整合适配。

---

## 8. 参考

- 源码：`IdsApi`、`SnowflakeIDGenImpl`、`SegmentIDGenImpl`
- Leaf 文档：https://github.com/Meituan-Dianping/Leaf
