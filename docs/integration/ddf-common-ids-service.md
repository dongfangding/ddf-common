# ddf-common-ids-service 接入指南

> 分布式 ID 生成：统一 `IdsApi` 入口，支持雪花（keyless）与号段（key-based）两种算法，通过 `IdGenRegistry` 按 `supportsKey()` 收集策略并分发。

## 核心能力

| 能力 | 说明 | 关键类 / 入口 |
|------|------|--------------|
| 统一 API | 单个 / 批量 / 组合 / 解析 ID | `api.IdsApi` |
| 生成器接口 | 顶层策略，`supportsKey()` 区分 key-based / keyless | `service.IDGen` |
| 策略注册表 | 收集 key-based 实现并按 key 分发 | `service.IdGenRegistry` |
| 雪花算法 | Zookeeper 分配 workerId，keyless | `service.impl.snowflake.SnowflakeIDGenImpl` |
| 号段模式 | 双 Segment 缓存 + 数据库分配，key-based | `service.impl.segment.SegmentIDGenImpl` |
| 配置属性 | 雪花 / 号段开关与参数 | `config.properties.IdsProperties` |

## 接入方式

### 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ids-service</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 雪花算法依赖 `org.apache.curator`（Zookeeper）；号段模式依赖 `DataSource`（需上层提供数据源）。

ID 生成组件由 `IdsServiceAutoConfiguration` 通过 `META-INF/spring/...AutoConfiguration.imports` 自动装配，引入依赖后**无需额外注解**，按配置开关启用对应算法。

### 关键配置

配置前缀 `customizer.infra.ids`（对应 `IdsProperties`）：

```yaml
customizer:
  infra:
    ids:
      name: "customizer.infra.ids"   # 雪花 ID 在 ZK 中的前缀节点
      begin-timestamp: 1609430400000 # 雪花起始时间戳（毫秒），混淆 ID 中的时间
      zk-address: "localhost:2181"   # Zookeeper 地址（雪花算法必需，支持集群）
      port: 2181                     # 标识机器的端口，同机伪集群时区分用
      snowflake-enable: true         # 是否开启雪花算法
      segment-enable: true           # 是否开启号段模式
```

> 号段模式的数据源通过 Spring `DataSource` Bean 注入（`IDAllocDaoImpl`），无独立 JDBC URL 配置项。

### 获取 ID

```java
@Autowired
private IdsApi idsApi;

// 雪花 ID（需 snowflake-enable=true）
String snowflakeId = idsApi.getSnowflakeId();
List<String> snowflakeIds = idsApi.getSnowflakeIds(100);

// 号段 ID（需 segment-enable=true），key 对应号段表 tag
String segmentId = idsApi.getSegmentId("order");
List<String> segmentIds = idsApi.getSegmentIds("order", 100);

// 组合 ID（号段 + 雪花）
IdsMultiData multiId = idsApi.getMultiId("order");

// 解析雪花 ID
DecodeSnowflakeIdData decoded = idsApi.decodeSnowflakeId(snowflakeId);
```

## 扩展点

### 1. IDGen 策略注册（自定义生成器）

实现 `IDGen` 并注册 Bean，即被 `IdGenRegistry` 收集。**key-based 实现返回 `supportsKey() = true` 参与按 key 分发，雪花等 keyless 实现返回 `false` 不参与**：

```java
@Component
public class RedisIDGen implements IDGen {

    @Override
    public boolean supportsKey() {
        return true;   // key-based 实现返回 true；keyless 返回 false
    }

    @Override
    public Result get(String key) {
        // 按 key 生成单个 ID
        return new Result(generate(key), Status.SUCCESS);
    }

    @Override
    public ResultList list(String key, int number) {
        // 按 key 批量生成 ID
        return resultList;
    }

    @Override
    public boolean init() {
        return true;
    }
}
```

`IdGenRegistry` 提供三个方法：

| 方法 | 说明 |
|------|------|
| `get(String key)` | 遍历 key-based 实现，返回第一个非空 `Result` |
| `list(String key, int number)` | 遍历 key-based 实现，批量生成 |
| `<T extends IDGen> T find(Class<T>)` | 按类型查找实现（如 `find(SegmentIDGenImpl.class)`） |

## 注意事项

1. **算法开关**：`snowflakeEnable` / `segmentEnable` 均为 `false` 时，对应 Bean 不注册，调用会失败。
2. **雪花依赖 ZK**：`SnowflakeIDGenImpl.init()` 通过 Zookeeper 分配 workerId，`zk-address` 必须配置；workerId 上限 1023。
3. **时钟回拨**：雪花算法检测到时钟回拨超过 5ms 会抛 `BusinessException(CLOCK_BACK)`。
4. **号段依赖 DB**：号段模式需要 `DataSource` 与号段表（`IDAllocDao`），tag 需预先在 DB 中配置；每次从 DB 取 `step` 个 ID，应用重启可能造成已分配 ID 的浪费。
5. **批量入参**：批量接口 `number` 为 0 或负数会抛异常（`LengthZeroException` / `BATCH_NUMBER_IS_VALID`）。
