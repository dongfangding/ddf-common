# CLAUDE.md

## 模块简介

ShardingSphere 集成模块，提供数据库分片功能。

## 核心类

| 类路径                                                        | 功能   |
|------------------------------------------------------------|------|
| `com.ddf.boot.common.sharding.config.ShardingProperties`   | 分片配置 |
| `com.ddf.boot.common.sharding.algorithm.ShardingAlgorithm` | 分片算法 |

## 使用说明

### 配置

```yaml
ddf:
  sharding:
    # 分片配置
```

### 读写分离配置

```java
@Bean
public DataSource dataSource() {
    // 配置读写分离数据源
}
```

## 注意事项

1. **分片键选择**：合理选择分片键
2. **跨分片查询**：避免全量跨分片查询
