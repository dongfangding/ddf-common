# ddf-common-es

> Elasticsearch dependency integration module. Aggregates `spring-boot-starter-data-elasticsearch` and provides a lightweight client wrapper,
> serving as the foundational dependency layer for higher-level Elasticsearch business modules.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-es` solves the **"unified Elasticsearch client dependency management"** problem.

| Scenario              | Typical Problem                                                        | What the Module Provides                            |
|-----------------------|------------------------------------------------------------------------|-----------------------------------------------------|
| Multi-module ES usage | Each business module imports its own ES client with different versions | Unified version and configuration entry             |
| Full-text search      | Need to develop based on Spring Data Elasticsearch                     | Auto-configuration and client wrapper               |
| Log / metrics storage | Business data needs to be written to ES indices                        | `EsClient` simplifies index and document operations |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-es</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
ddf:
  elasticsearch:
    host: localhost
    port: 9200
```

Or use Spring Boot standard configuration directly:

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

---

## 4. Core API

### 4.1 EsClient index operations

```java
@Autowired
private EsClient esClient;

// Create index
esClient.createIndex("order_index");

// Check index exists
boolean exists = esClient.indexExists("order_index");

// Delete index
esClient.deleteIndex("order_index");
```

### 4.2 Spring Data Elasticsearch

This module imports `spring-boot-starter-data-elasticsearch`, so you can use Spring Data Repository pattern directly:

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

## 5. Advanced Usage / Extension Points

### 5.1 Index templates and mappings

Customize index settings and mappings via `EsClient`:

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

### 5.2 Cluster configuration

```yaml
spring:
  elasticsearch:
    uris:
      - http://es-node1:9200
      - http://es-node2:9200
      - http://es-node3:9200
```

---

## 6. Interplay with Other Modules

| Module            | How They Cooperate                                                                   |
|-------------------|--------------------------------------------------------------------------------------|
| Business modules  | Build business-level indexing, querying, and aggregation logic on top of this module |
| `ddf-common-core` | JSON serialization, date formatting, and other utility support                       |

---

## 7. FAQ

**Q1: Is this module a full ES business SDK?**
Currently it is primarily a dependency-layer wrapper with a lightweight client. Business-level index management, query DSL abstractions, and aggregation logic should be built in business modules or later dedicated modules.

**Q2: ES version compatibility?**
This module follows the ES client version bundled with Spring Boot 3.x. Ensure your server version is compatible with the client (usually ES 7.x/8.x are both fine).

**Q3: Do I need to import RestHighLevelClient separately?**
Spring Boot 3.x has migrated to the new Java API Client. This module automatically brings it in via `spring-boot-starter-data-elasticsearch`; no manual import of the legacy client is needed.

---

## 8. References

- Source: `ElasticSearchProperties`, `EsClient`
- Spring Data Elasticsearch: https://docs.spring.io/spring-data/elasticsearch/reference/
