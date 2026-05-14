# ddf-common-sharding

> ShardingSphere 集成模块。基于 ShardingSphere-JDBC 5.4.0 提供数据库分片、读写分离和分布式事务能力，
> 兼容 Spring Boot 3.x 和 Jakarta EE 命名空间。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-sharding` 解决的是 **"单库性能瓶颈，需要水平扩展数据库"** 问题。

| 场景     | 典型问题           | 模块提供的能力                   |
|--------|----------------|---------------------------|
| 海量订单数据 | 单表数据过亿，查询和维护困难 | 水平分表，按用户 ID 或时间维度的散列拆分    |
| 高并发写入  | 单库 TPS 达到上限    | 分库分表将压力分散到多个数据节点          |
| 读多写少业务 | 查询拖垮主库         | 读写分离，查询走从库                |
| 分布式事务  | 跨库操作需要保证一致性    | ShardingSphere AT 模式分布式事务 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-sharding</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 版本要求：Spring Boot 3.5.x、ShardingSphere 5.4.0、Java 17+。

---

## 3. 最小化配置

### 3.1 数据源配置

在 `application.yml` 中引用 ShardingSphere 配置文件：

```yaml
spring:
  datasource:
    driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver
    url: jdbc:shardingsphere:classpath:sharding.yaml
```

### 3.2 分片规则配置

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

---

## 4. 核心 API

### 4.1 分片算法

ShardingSphere 内置多种分片算法，按需选择：

| 算法         | 类型   | 说明                |
|------------|------|-------------------|
| `MOD`      | 标准分片 | 取模散列，数据分布均匀       |
| `HASH_MOD` | 标准分片 | 先哈希再取模，避免连续 ID 热点 |
| `RANGE`    | 标准分片 | 按范围分片，适合时间维度      |
| `INLINE`   | 内联分片 | Groovy 表达式自定义路由规则 |

### 4.2 读写分离配置示例

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

### 4.3 分布式事务（AT 模式）

```yaml
rules:
  transaction:
    defaultType: XA
    providerType: Atomikos
```

> 业务代码中正常使用 `@Transactional` 注解即可，ShardingSphere 底层自动处理跨库事务协调。

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义分片算法

实现 `StandardShardingAlgorithm` 接口：

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

### 5.2 绑定表与广播表

```yaml
rules:
  sharding:
    bindingTables:
      - t_order,t_order_item    # 绑定表：关联查询避免笛卡尔积
    broadcastTables:
      - t_config                # 广播表：每个节点全量复制
```

### 5.3 数据加密

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

## 6. 与其他模块协作

| 模块                              | 协作方式                                    |
|---------------------------------|-----------------------------------------|
| `ddf-common-data-mysql-starter` | 提供数据源和连接池基础能力，ShardingSphere 在其之上做路由和代理 |
| `ddf-common-ids-service`        | 分片表建议使用雪花算法生成全局唯一主键                     |
| `ddf-common-zookeeper`          | 如需使用 ShardingSphere 集群模式，可结合 ZK 做配置中心   |

---

## 7. FAQ

**Q1：与 ShardingSphere-Proxy 有什么区别？**
本模块使用 ShardingSphere-JDBC（客户端代理），以 jar 包形式嵌入应用，无额外中间件部署成本。Proxy 是独立进程，适合多语言异构接入。

**Q2：Spring Boot 3.x 迁移注意事项？**

1. 使用 `jakarta.sql.DataSource` 替代 `javax.sql.DataSource`
2. MySQL 驱动使用 `mysql-connector-j` 而非 `mysql-connector-java`
3. 确保连接池兼容（HikariCP、Druid）

**Q3：跨分片查询性能如何？**
尽量避免全量跨分片查询（如不带分片键的 `SELECT *`）。ShardingSphere 会聚合多个节点的结果，数据量大时性能下降明显。

**Q4：分布式事务 AT 模式有什么限制？**
AT 模式基于 SQL 解析生成反向 SQL 做回滚，不支持无主键表、不支持某些复杂 SQL（如 `UPDATE ... LIMIT`）。建议核心业务仍使用本地事务，跨库事务用于可补偿场景。

---

## 8. 参考

- 源码：`ShardingAutoConfiguration`、`SpringBootPropertiesConfiguration`、`LocalRulesCondition`
- ShardingSphere 官方文档：https://shardingsphere.apache.org/
- ShardingSphere JDBC 快速开始：https://shardingsphere.apache.org/document/5.4.0/cn/quick-start/shardingsphere-jdbc-quick-start/
