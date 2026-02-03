# ddf-common-sharding

ShardingSphere 集成模块，提供数据库分片、读写分离、分布式事务功能。

## 功能特性

- **分库分表**: 支持水平分片、垂直分片
- **读写分离**: 主从复制读写分离
- **分布式事务**: AT 模式分布式事务
- **数据加密**: 列级数据加密
- **分布式序列**: 雪花算法主键生成

## 版本要求

| 依赖 | 版本 |
|-----|------|
| Spring Boot | 3.5.x |
| ShardingSphere | 5.4.0 |
| Java | 17+ |

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-sharding</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 快速开始

### 1. 配置数据源

在 `application.yml` 中配置：

```yaml
spring:
  datasource:
    driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver
    url: jdbc:shardingsphere:classpath:sharding.yaml
```

### 2. 创建 Sharding 配置

创建 `src/main/resources/sharding.yaml`：

```yaml
dataSources:
  ds_master:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/db_master?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: password

rules:
  sharding:
    tables:
      t_order:
        actualDataNodes: ds_master.t_order_${0..15}
        tableStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: t_order_algorithm
        keyGenerateStrategy:
          type: SNOWFLAKE
          column: id
          keyGeneratorName: snowflake
    shardingAlgorithms:
      t_order_algorithm:
        type: MOD
        props:
          sharding-count: 16
    keyGenerators:
      snowflake:
        type: SNOWFLAKE
        props:
          worker-id: 1

props:
  sql-show: true
```

### 3. 启用自动配置

```java
@SpringBootApplication
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 配置示例

完整配置示例参考 `example-sharding.yaml`，包含：

- 读写分离配置
- 分库分表配置
- 数据加密配置
- 分布式序列配置

## 核心类

| 类路径 | 功能 |
|-------|------|
| `com.ddf.boot.common.sharding.config.ShardingAutoConfiguration` | 自动配置类 |
| `com.ddf.boot.common.sharding.prop.SpringBootPropertiesConfiguration` | 配置属性 |
| `com.ddf.boot.common.sharding.rule.LocalRulesCondition` | 本地规则条件 |

## 迁移指南

### 从 ShardingSphere 5.2.x 迁移

1. **更新依赖版本**:
   ```xml
   <sharding-jdbc.version>5.4.0</sharding-jdbc.version>
   ```

2. **Jakarta 命名空间**:
   ```java
   // javax.sql.DataSource → jakarta.sql.DataSource
   import jakarta.sql.DataSource;
   ```

3. **配置方式变更**:
   - 推荐使用 YAML 配置文件
   - 通过 `spring.datasource.url` 引用配置文件

### Spring Boot 3.x 注意事项

1. 使用 `jakarta.sql.DataSource` 替代 `javax.sql.DataSource`
2. MySQL 驱动使用 `mysql-connector-j` 而非 `mysql-connector-java`
3. 确保使用兼容的连接池（HikariCP、Druid）

## 注意事项

1. **分片键选择**: 合理选择分片键，避免热点数据
2. **跨分片查询**: 避免全量跨分片查询
3. **分布式事务**: 谨慎使用，AT 模式有性能损耗
4. **SQL 兼容性**: 部分复杂 SQL 可能不支持

## 参考资料

- [ShardingSphere 官方文档](https://shardingsphere.apache.org/)
- [ShardingSphere JDBC 快速开始](https://shardingsphere.apache.org/document/5.4.0/cn/quick-start/shardingsphere-jdbc-quick-start/)
