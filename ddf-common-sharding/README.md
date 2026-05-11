# ddf-common-sharding

> ShardingSphere integration module. Based on ShardingSphere-JDBC 5.4.0, provides database sharding, read/write splitting,
> and distributed transaction capabilities, compatible with Spring Boot 3.x and Jakarta EE namespace.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-sharding` solves the **"single-database performance bottleneck requires horizontal scaling"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Massive order data | Single table exceeds 100M rows; queries and maintenance are difficult | Horizontal table sharding, hash splitting by user ID or time dimension |
| High-concurrency writes | Single-database TPS has reached its limit | Database and table sharding distributes pressure across multiple data nodes |
| Read-heavy, write-light business | Queries overwhelm the primary database | Read/write splitting; queries go to replicas |
| Distributed transactions | Cross-database operations need consistency | ShardingSphere AT-mode distributed transactions |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-sharding</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> Version requirements: Spring Boot 3.5.x, ShardingSphere 5.4.0, Java 17+.

---

## 3. Minimum Configuration

### 3.1 Data Source Configuration

Reference the ShardingSphere configuration file in `application.yml`:

```yaml
spring:
  datasource:
    driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver
    url: jdbc:shardingsphere:classpath:sharding.yaml
```

### 3.2 Sharding Rule Configuration

Create `src/main/resources/sharding.yaml`:

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

---

## 4. Core API

### 4.1 Sharding Algorithms

ShardingSphere provides multiple built-in sharding algorithms:

| Algorithm | Type | Description |
| --- | --- | --- |
| `MOD` | Standard | Modulo hashing, evenly distributed |
| `HASH_MOD` | Standard | Hash then modulo, avoids sequential ID hotspot |
| `RANGE` | Standard | Range-based sharding, suitable for time dimensions |
| `INLINE` | Inline | Groovy expression for custom routing rules |

### 4.2 Read/Write Splitting Example

```yaml
rules:
  readwrite-splitting:
    dataSources:
      ds_readwrite:
        type: Static
        props:
          write-data-source-name: ds_master
          read-data-source-names: ds_slave1,ds_slave2
        loadBalancerName: round_robin
    loadBalancers:
      round_robin:
        type: ROUND_ROBIN
```

### 4.3 Distributed Transactions (AT Mode)

```yaml
rules:
  transaction:
    defaultType: XA
    providerType: Atomikos
```

> Use the `@Transactional` annotation normally in business code; ShardingSphere handles cross-database transaction coordination automatically.

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom Sharding Algorithm

Implement the `StandardShardingAlgorithm` interface:

```java
@Component
public class CustomShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        Long userId = shardingValue.getValue();
        int index = (int) (userId % availableTargetNames.size());
        return new ArrayList<>(availableTargetNames).get(index);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        return availableTargetNames;
    }
}
```

### 5.2 Binding Tables and Broadcast Tables

```yaml
rules:
  sharding:
    bindingTables:
      - t_order,t_order_item    # Binding tables: avoid Cartesian product in joined queries
    broadcastTables:
      - t_config                # Broadcast table: fully replicated on every node
```

### 5.3 Data Encryption

```yaml
rules:
  encrypt:
    tables:
      t_user:
        columns:
          phone:
            cipherColumn: phone_cipher
            encryptorName: aes
    encryptors:
      aes:
        type: AES
        props:
          aes-key-value: your-secret-key
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-data-mysql-starter` | Provides data source and connection pool fundamentals; ShardingSphere adds routing and proxy on top |
| `ddf-common-ids-service` | Sharded tables are recommended to use Snowflake for globally unique primary keys |
| `ddf-common-zookeeper` | For ShardingSphere cluster mode, ZK can be used as a configuration center |

---

## 7. FAQ

**Q1: What's the difference from ShardingSphere-Proxy?**
This module uses ShardingSphere-JDBC (client-side proxy), embedded as a jar in the application with no additional middleware deployment cost. Proxy is a standalone process, suitable for multi-language heterogeneous access.

**Q2: What to watch for in Spring Boot 3.x migration?**
1. Use `jakarta.sql.DataSource` instead of `javax.sql.DataSource`
2. MySQL driver is `mysql-connector-j` instead of `mysql-connector-java`
3. Ensure connection pool compatibility (HikariCP, Druid)

**Q3: How is cross-shard query performance?**
Avoid full cross-shard queries (e.g., `SELECT *` without sharding key) as much as possible. ShardingSphere aggregates results from multiple nodes; performance degrades significantly with large data volumes.

**Q4: What are the limitations of AT-mode distributed transactions?**
AT mode generates reverse SQL based on SQL parsing for rollback. It does not support tables without primary keys and certain complex SQL (e.g., `UPDATE ... LIMIT`). For core business, prefer local transactions; use cross-database transactions for compensable scenarios.

---

## 8. References

- Source: `ShardingAutoConfiguration`, `SpringBootPropertiesConfiguration`, `LocalRulesCondition`
- ShardingSphere official docs: https://shardingsphere.apache.org/
- ShardingSphere JDBC quick start: https://shardingsphere.apache.org/document/5.4.0/en/quick-start/shardingsphere-jdbc-quick-start/
