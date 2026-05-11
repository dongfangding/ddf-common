# ddf-common-es

> Elasticsearch 依赖集成模块。聚合 `spring-boot-starter-data-elasticsearch` 并提供轻量客户端封装，
> 作为上层 Elasticsearch 业务模块的基础依赖层。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-es` 解决的是 **"Elasticsearch 客户端依赖统一管理"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| --- | --- | --- |
| 多模块共用 ES | 各业务模块各自引入 ES 客户端，版本不一致 | 统一版本、统一配置入口 |
| 全文检索业务 | 需要基于 Spring Data Elasticsearch 开发 | 提供自动配置与客户端封装 |
| 日志/指标存储 | 业务数据需要写入 ES 索引 | `EsClient` 简化索引与文档操作 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-es</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  elasticsearch:
    host: localhost
    port: 9200
```

或直接使用 Spring Boot 标准配置：

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

---

## 4. 核心 API

### 4.1 EsClient 操作索引

```java
@Autowired
private EsClient esClient;

// 创建索引
esClient.createIndex("order_index");

// 检查索引是否存在
boolean exists = esClient.indexExists("order_index");

// 删除索引
esClient.deleteIndex("order_index");
```

### 4.2 Spring Data Elasticsearch

模块已引入 `spring-boot-starter-data-elasticsearch`，可直接使用 Spring Data 的 Repository 模式：

```java
@Document(indexName = "products")
public class Product {
    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Double)
    private BigDecimal price;
}

public interface ProductRepository extends ElasticsearchRepository<Product, String> {
    List<Product> findByNameContaining(String name);
}
```

---

## 5. 进阶用法 / 扩展点

### 5.1 索引模板与映射

通过 `EsClient` 自定义索引设置和映射：

```java
esClient.createIndexWithMapping("logs", "{
  \"mappings\": {
    \"properties\": {
      \"timestamp\": { \"type\": \"date\" },
      \"level\": { \"type\": \"keyword\" },
      \"message\": { \"type\": \"text\" }
    }
  }
}");
```

### 5.2 集群配置

```yaml
spring:
  elasticsearch:
    uris:
      - http://es-node1:9200
      - http://es-node2:9200
      - http://es-node3:9200
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| --- | --- |
| 业务模块 | 在本模块之上构建业务索引、查询、聚合逻辑 |
| `ddf-common-core` | JSON 序列化、日期格式化等工具支撑 |

---

## 7. FAQ

**Q1：本模块是完整的 ES 业务 SDK 吗？**
目前主要是依赖层封装和轻量客户端。业务层的索引管理、查询 DSL 封装、聚合逻辑等建议在业务模块或后续专用模块中构建。

**Q2：ES 版本兼容性？**
模块跟随 Spring Boot 3.x 的 ES 客户端版本。请确保服务端版本与客户端兼容（通常 ES 7.x/8.x 均可）。

**Q3：是否需要额外引入 RestHighLevelClient？**
Spring Boot 3.x 已迁移到新版 Java API Client，本模块通过 `spring-boot-starter-data-elasticsearch` 自动引入，无需手动添加旧版客户端。

---

## 8. 参考

- 源码：`ElasticSearchProperties`、`EsClient`
- Spring Data Elasticsearch：https://docs.spring.io/spring-data/elasticsearch/reference/
