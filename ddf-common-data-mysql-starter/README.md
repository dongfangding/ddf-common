# ddf-common-data-mysql-starter

> MySQL data infrastructure starter. Aggregates `spring-boot-starter-jdbc` + `mysql-connector-j` + `druid-spring-boot-3-starter`
> into a single dependency, so application services don't have to manually assemble the data layer.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-data-mysql-starter` solves the **"data-layer dependency standardization"** problem.

| Category | Typical Problem | What the Module Provides |
| ----- | ----- | ----- |
| New project bootstrap | Hand-writing jdbc/mysql/druid dependencies every time | One starter brings in the entire data-layer foundation |
| Tech stack alignment | Inconsistent Druid / MySQL driver versions across projects | Versions centrally managed in `ddf-common-dependency` BOM |
| Druid compatibility | `usePingMethod` causing connection validation errors under Spring Boot 3.x | Auto-sets `druid.mysql.usePingMethod=false` |
| MyBatis integration | Need to manually add MyBatis Spring Boot Starter | Included transitively, ready to use out of the box |

> ⚠️ This module does **not** include ORM mapping, code generation, or sharding capabilities.
> For sharding, add `ddf-common-sharding` separately.

---

## 2. Maven Dependency

Direct dependency:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Or through the default starter (already included):

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Transitive dependencies:

- `spring-boot-starter-jdbc`
- `mysql-connector-j`
- `druid-spring-boot-3-starter`
- `mybatis-spring-boot-starter`

---

## 3. Minimum Configuration

### 3.1 Data source connection

Continue using standard Spring Boot configuration:

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
```

### 3.2 Starter-specific configuration

```yaml
customizer:
  data:
    mysql:
      enabled: true                    # Master switch, default true
      druid:
        use-ping-method: false         # Maps to druid.mysql.usePingMethod, default false
```

| Property | Description | Default |
| ----- | ----- | ----- |
| `enabled` | Whether to enable this starter's data-layer enhancements | `true` |
| `druid.use-ping-method` | Whether Druid uses `mysql_ping` for connection validation | `false` |

> Rationale for `use-ping-method: false`: under certain MySQL server configurations, `mysql_ping`
> causes connection state validation errors; SQL probe (`SELECT 1`) is more compatible.

### 3.3 Detailed Druid configuration

Druid-specific parameters are still configured via `spring.datasource.druid.*`:

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

## 4. Core API Guide

This module is an **infrastructure starter** and does not expose business APIs directly.
After importing, the following are automatically set up:

1. **DataSource registration**: `DruidDataSource` is automatically injected into the Spring container
2. **MyBatis ready**: `SqlSessionFactory`, `SqlSessionTemplate`, and `MapperScannerConfigurer` are auto-configured
3. **JDBC Template ready**: `JdbcTemplate` and `NamedParameterJdbcTemplate` can be `@Autowired` directly
4. **Druid monitoring**: Visit `/druid/index.html` to view connection pool status (requires `stat-view-servlet` to be enabled)

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

## 5. Advanced Usage / Extension Points

### 5.1 Multiple data sources

Standard Spring Boot multi-datasource configuration works; Druid applies independently to each `DataSource`:

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

### 5.2 Replacing Druid with HikariCP

If your business doesn't need Druid's monitoring and SQL injection prevention features,
exclude Druid and add HikariCP:

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

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| ----- | ----- |
| `ddf-common-dependency` | Versions centrally declared in BOM; child modules omit version tags |
| `ddf-common-core` | `BaseDomain` entity base and `PageUtil` pagination utilities work with MyBatis |
| `ddf-common-sharding` | Add ShardingSphere configuration for sharding scenarios |
| `ddf-common-starter-default` | This module is already included in the default starter |
| `ddf-common-governance-starter` | If Actuator is enabled, Druid monitoring data can be aggregated and exposed |

---

## 7. FAQ

**Q1: Does this starter conflict with `spring-boot-starter-data-jpa`?**  
No. This starter only provides data source and connection pool infrastructure.
For JPA, add `spring-boot-starter-data-jpa` separately; both share the same `DataSource`.

**Q2: Druid monitoring page returns 404?**  
Confirm `spring.datasource.druid.stat-view-servlet.enabled: true`, and ensure Spring Security
is not intercepting `/druid/**`. In production, restrict access via IP whitelist or Basic Auth.

**Q3: What happens if `use-ping-method` is set to `true`?**  
Druid will use the native MySQL `mysql_ping` for connection liveness checks. Some cloud databases
or proxy middleware (e.g. MyCat) have incomplete support for this protocol, which can cause
connections to be incorrectly judged as dead and recreated frequently.

**Q4: Why is `@MapperScan` not needed even though MyBatis is included?**  
`mybatis-spring-boot-starter` automatically scans `@Mapper` interfaces within the startup class
package and its sub-packages. If Mappers reside in external jars, explicitly configure `@MapperScan`
on the main class.

**Q5: How do I disable all enhancements from this starter?**  
```yaml
customizer:
  data:
    mysql:
      enabled: false
```
Or exclude the auto-configuration class:
```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.data.mysql.config.DataMysqlAutoConfiguration
```

---

## 8. References

- Source: `config/DataMysqlAutoConfiguration.java`, `config/DataMysqlProperties.java`
- Druid docs: https://github.com/alibaba/druid
- MyBatis Spring Boot Starter docs: https://mybatis.org/spring-boot-starter/
