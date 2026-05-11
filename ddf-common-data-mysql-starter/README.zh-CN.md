# ddf-common-data-mysql-starter

> MySQL 数据基础设施 starter。统一聚合 `spring-boot-starter-jdbc` + `mysql-connector-j` + `druid-spring-boot-3-starter`，
> 将数据接入技术栈固化到一个依赖中，业务服务无需手工拼装。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-data-mysql-starter` 解决的是 **"数据层依赖标准化"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| ----- | ----- | ----- |
| 新项目启动 | 每次都要手写 jdbc/mysql/druid 三件套依赖 | 一个 starter 引入全部数据层基础组件 |
| 技术栈统一 | 团队各项目 Druid / MySQL 驱动版本不一致 | 版本在 `ddf-common-dependency` BOM 中集中管理 |
| Druid 兼容性 | Spring Boot 3.x 下 Druid 的 `usePingMethod` 引发连接检测异常 | 自动设置 `druid.mysql.usePingMethod=false` |
| MyBatis 集成 | 需要额外引入 MyBatis Spring Boot Starter | 已作为传递依赖引入，开箱即用 |

> ⚠️ 本模块**不包含** ORM 映射、代码生成或分库分表能力。如需分片，请额外引入 `ddf-common-sharding`。

---

## 2. 依赖引入

直接依赖本 starter：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

或通过默认 starter（已包含）：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

传递依赖：

- `spring-boot-starter-jdbc`
- `mysql-connector-j`
- `druid-spring-boot-3-starter`
- `mybatis-spring-boot-starter`

---

## 3. 最小化配置

### 3.1 数据源连接

继续使用 Spring Boot 标准配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
```

### 3.2 Starter 专属配置

```yaml
customizer:
  data:
    mysql:
      enabled: true                    # 总开关，默认 true
      druid:
        use-ping-method: false         # 对应 druid.mysql.usePingMethod，默认 false
```

| 属性 | 说明 | 默认值 |
| ----- | ----- | ----- |
| `enabled` | 是否启用本 starter 的数据层增强 | `true` |
| `druid.use-ping-method` | Druid 连接检测是否使用 `mysql_ping` | `false` |

> `use-ping-method: false` 的缘由：某些 MySQL 服务端配置下，`mysql_ping` 会导致连接状态检测异常，改为 SQL 探测（`SELECT 1`）兼容性更好。

### 3.3 Druid 详细配置

Druid 专属参数仍通过 `spring.datasource.druid.*` 配置：

```yaml
spring:
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      validation-query: SELECT 1
      web-stat-filter:
        enabled: true
      stat-view-servlet:
        enabled: true
        login-username: admin
        login-password: admin
```

---

## 4. 核心 API 使用指南

本模块为**基础设施 starter**，不直接提供业务 API。引入后自动完成以下事项：

1. **DataSource 注册**：`DruidDataSource` 自动注入 Spring 容器
2. **MyBatis 就绪**：`SqlSessionFactory`、`SqlSessionTemplate`、`MapperScannerConfigurer` 自动配置
3. **JDBC Template 就绪**：`JdbcTemplate`、`NamedParameterJdbcTemplate` 可直接 `@Autowired` 使用
4. **Druid 监控**：访问 `/druid/index.html` 查看连接池状态（需开启 `stat-view-servlet`）

```java
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);
}

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public List<User> queryByJdbc(String name) {
        return jdbcTemplate.query(
            "SELECT * FROM user WHERE name = ?",
            new BeanPropertyRowMapper<>(User.class), name
        );
    }
}
```

---

## 5. 进阶用法 / 扩展点

### 5.1 多数据源

Spring Boot 标准多数据源配置即可，Druid 对每个 DataSource 独立生效：

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DruidDataSourceBuilder.create().build();
    }
}
```

### 5.2 替换 Druid 为 HikariCP

若业务不需要 Druid 的监控和防SQL注入功能，排除 Druid 并引入 HikariCP：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
    <exclusions>
        <exclusion>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| ----- | ----- |
| `ddf-common-dependency` | 版本在 BOM 中统一声明，子模块引用不写版本号 |
| `ddf-common-core` | `BaseDomain` 实体基类、`PageUtil` 分页工具与 MyBatis 配合使用 |
| `ddf-common-sharding` | 分库分表场景下叠加 ShardingSphere 配置 |
| `ddf-common-starter-default` | 默认 starter 已包含本模块 |
| `ddf-common-governance-starter` | 若开启 Actuator，Druid 监控数据可被聚合暴露 |

---

## 7. FAQ

**Q1：本 starter 和 `spring-boot-starter-data-jpa` 冲突吗？**  
不冲突。本 starter 只提供数据源和连接池基础设施；如需 JPA，额外引入 `spring-boot-starter-data-jpa` 即可，两者共用同一 `DataSource`。

**Q2：Druid 监控页面 404 怎么办？**  
确认 `spring.datasource.druid.stat-view-servlet.enabled: true`，且没有 Spring Security 拦截 `/druid/**` 路径。生产环境建议通过 IP 白名单或 Basic Auth 限制访问。

**Q3：`use-ping-method` 设为 `true` 会怎样？**  
Druid 将使用 MySQL 原生的 `mysql_ping` 进行连接存活检测。某些云数据库或代理中间件（如 MyCat）对该协议支持不完整，可能导致连接被误判为失效而频繁重建。

**Q4：为什么引入了 MyBatis 但不需要写 `@MapperScan`？**  
`mybatis-spring-boot-starter` 会自动扫描 `@Mapper` 接口，默认扫描范围为启动类所在包及其子包。如果 Mapper 位于外部 jar，需在启动类显式配置 `@MapperScan`。

**Q5：如何关闭本 starter 的所有增强？**  
```yaml
customizer:
  data:
    mysql:
      enabled: false
```
或排除自动配置类：
```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.data.mysql.config.DataMysqlAutoConfiguration
```

---

## 8. 参考

- 源码：`config/DataMysqlAutoConfiguration.java`、`config/DataMysqlProperties.java`
- Druid 官方文档：https://github.com/alibaba/druid
- MyBatis Spring Boot Starter 文档：https://mybatis.org/spring-boot-starter/
