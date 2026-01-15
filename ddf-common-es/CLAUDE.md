# CLAUDE.md

## 模块简介

Elasticsearch 集成模块，提供 ES 客户端和操作封装。

## 核心类

| 类路径                                                     | 功能      |
|---------------------------------------------------------|---------|
| `com.ddf.boot.common.es.config.ElasticSearchProperties` | ES 配置属性 |
| `com.ddf.boot.common.es.client.EsClient`                | ES 客户端  |

## 使用说明

### 配置

```yaml
ddf:
  elasticsearch:
    host: localhost
    port: 9200
```

### 创建索引

```java
@Autowired
private EsClient esClient;

public void createIndex(String indexName) {
    esClient.createIndex(indexName);
}
```

## 注意事项

1. **版本兼容**：确保 ES 版本与客户端兼容
2. **索引设计**：合理设计索引映射和分片
