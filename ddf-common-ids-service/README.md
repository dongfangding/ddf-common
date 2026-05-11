# ddf-common-ids-service

> Distributed ID generation service module. Supports both Snowflake and Segment modes,
> providing high-performance, highly available globally unique IDs for distributed systems.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-ids-service` solves the **"how to generate globally unique IDs in distributed environments"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Sharded database primary keys | Auto-increment IDs conflict after sharding | Snowflake generates globally unique, trend-increasing IDs |
| Order number generation | Need short, ordered, non-repeating order numbers | Segment mode allocates in batches by business code |
| High-concurrency writes | Database auto-increment becomes a bottleneck | Local cache of ID segments reduces DB access |
| Data migration | ID conflicts after merging multiple data centers | Snowflake isolates via data-center bits |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ids-service</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

### Snowflake mode

```yaml
customizer:
  infra:
    ids:
      snowflakeEnable: true
      beginTimestamp: 1609430400000
      zkAddress: 127.0.0.1:2181
      port: 2181
```

### Segment mode

```yaml
customizer:
  infra:
    ids:
      segmentEnable: true
      # Segment mode requires a data source; configure DB connection
```

---

## 4. Core API

### 4.1 Snowflake ID

```java
@Autowired
private IdsApi idsApi;

// Single ID
String id = idsApi.getSnowflakeId();   // e.g. 1785643298765432123

// Batch IDs
List<String> ids = idsApi.getSnowflakeIds(100);
```

### 4.2 Segment ID

```java
// Single ID by business type
String orderId = idsApi.getSegmentId("order");

// Batch IDs
List<String> orderIds = idsApi.getSegmentIds("order", 100);

// Using business code enum
String id = idsApi.getSegmentId(BizCode.ORDER);
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Algorithm comparison

| Feature | Snowflake | Segment |
| --- | --- | --- |
| Dependency | Zookeeper | Database |
| Performance | Extremely high (local generation) | High (local segment cache) |
| Ordering | Trend-increasing | Strictly increasing |
| Suitable for | High concurrency, distributed | Medium concurrency, strict continuity needed |
| Clock sensitivity | Sensitive (needs handling) | Not sensitive |

### 5.2 Snowflake structure

```
| 1 sign bit | 41 timestamp bits | 5 data-center bits | 5 worker bits | 12 sequence bits |
```

- Timestamp range: ~69 years
- Data centers: 32
- Workers: 32 per data center
- Sequence: 4096 per millisecond

### 5.3 Custom segment table

Segment mode can use a custom `IDAllocDao` to plug in your own segment allocation table:

```java
@Component
public class CustomIDAllocDao implements IDAllocDao {
    @Override
    public SegmentBuffer getBuffer(String bizTag) {
        // Query segment from custom table
    }
}
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-zookeeper` | Snowflake depends on ZK to allocate worker machine IDs |
| `ddf-common-data-mysql-starter` | Segment mode depends on DB to store segments |
| `ddf-common-core` | Time utilities, concurrency utilities, and other fundamentals |

---

## 7. FAQ

**Q1: How does Snowflake handle clock rollback?**
If a clock rollback exceeding 5ms is detected, the module throws an exception. It is recommended to use NTP with alerting configured.

**Q2: Are segment-mode IDs wasted?**
Possibly. Each time `step` IDs are fetched from the database and cached locally; unused segments at application restart are lost.

**Q3: Can both modes be enabled at the same time?**
Yes. Business code can choose `getSnowflakeId()` or `getSegmentId()` depending on the scenario.

**Q4: What open-source designs influenced this module?**
Inspired by Meituan Leaf, adapted and integrated into the current project.

---

## 8. References

- Source: `IdsApi`, `SnowflakeIDGenImpl`, `SegmentIDGenImpl`
- Leaf docs: https://github.com/Meituan-Dianping/Leaf
