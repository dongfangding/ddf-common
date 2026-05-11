# ddf-common-starter-default

> Default business scenario aggregated starter. Bundles web fundamentals, MySQL data access,
> and governance extensions into a single dependency — the recommended first choice for typical Spring Boot business applications.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Starter

`ddf-common-starter-default` solves the **"one-stop foundational capabilities for business applications"** problem.

| Scenario | Typical Problem | What This Starter Provides |
| --- | --- | --- |
| Conventional monolith / microservice | Repeating web, DB, and governance imports for every new project | One dependency, everything ready |
| CRUD backend service | Needs uniform response format + database + email alerts | Full out-of-the-box foundation |
| Rapid prototyping | Don't want to spend time on dependency configuration | Import and go, focus on business logic |
| Small-to-medium teams | Limited manpower to maintain foundational components | Unified versions, behaviors, and upgrades |

**Not suitable for**:
- Pure web gateway / database-less services → use `ddf-common-starter-web`
- Non-MySQL databases (PostgreSQL, Oracle, MongoDB, etc.) → use `ddf-common-starter-web` + custom data layer
- Web + DB only, no Mail / Actuator needed → use `ddf-common-starter-web` + `ddf-common-data-mysql-starter`

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> Version is managed by the `ddf-common-dependency` BOM; no explicit version needed in business projects.

---

## 3. Included Modules & Capabilities

This starter aggregates three lower-level starters:

### 3.1 `ddf-common-starter-web` (Web foundation)

| Sub-module | Core Capability |
| --- | --- |
| `ddf-common-api` | Uniform response `ResponseData`, business exception `BusinessException`, error-code system |
| `ddf-common-core` | Utilities (Hutool/Guava/Fastjson2), encryption, Snowflake IDs, local cache, thread pools |
| `ddf-common-mvc` | Global exception handling, auto response wrapping, Jackson config, user context `UserContextUtil` |
| `ddf-common-limit` | Token-bucket rate limiting `@RateLimit`, anti-replay `@Repeatable` |
| `ddf-common-log4j` | Log4j2 + Disruptor async logging (Logback excluded) |

### 3.2 `ddf-common-data-mysql-starter` (Data access)

| Capability | Description |
| --- | --- |
| JDBC + MySQL driver | `spring-boot-starter-jdbc` + `mysql-connector-j` |
| Druid connection pool | `druid-spring-boot-3-starter`, includes `usePingMethod` compatibility fix |
| MyBatis | `mybatis-spring-boot-starter` |

### 3.3 `ddf-common-governance-starter` (Governance)

| Capability | Description |
| --- | --- |
| Mail service | `MailService` / `DefaultMailService` / `MailUtil`, conditionally registered (requires `spring.mail.*` config) |
| Thread-pool metrics | `ThreadPoolMetricsBinder` auto-scans thread-pool beans and binds to Micrometer |

---

## 4. Minimum Configuration

### 4.1 Database configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/mydb?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
  mail:
    host: smtp.example.com
    username: no-reply@example.com
    password: your-password
    port: 587

customizer:
  data:
    mysql:
      enabled: true
      druid:
        use-ping-method: true
  governance:
    mail:
      enabled: true
```

> If `spring.mail.*` is not configured, `MailService` will not be registered. Importing the starter remains safe and will not fail.

### 4.2 Uniform response format (out of the box)

```java
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
        // Actual output: {"code":200,"message":"success","data":{"id":1,...}}
    }
}
```

### 4.3 Business exceptions

```java
@Service
public class OrderService {
    public Order create(OrderRequest request) {
        if (request.getAmount() == null) {
            throw new BusinessException(BaseErrorCallbackCode.BAD_REQUEST);
        }
        // ...
    }
}
```

### 4.4 Rate limiting

```java
@RateLimit(max = 100, rate = 60)
@GetMapping("/api/orders")
public List<Order> listOrders() {
    return orderService.list();
}
```

### 4.5 Anti-replay

```java
@Repeatable(interval = 5000)
@PostMapping("/api/orders")
public Order createOrder(@RequestBody OrderRequest request) {
    return orderService.create(request);
}
```

### 4.6 Sending mail

```java
@Service
public class AlertService {
    @Autowired
    private MailService mailService;

    public void sendAlert(String message) {
        mailService.sendSimpleMail("admin@example.com", "System Alert", message);
    }
}
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Starter selection decision tree

```
Need database?
├── No → ddf-common-starter-web
└── Yes → Need Mail / Actuator?
    ├── No → ddf-common-starter-web + ddf-common-data-mysql-starter
    └── Yes → ddf-common-starter-default (recommended)
```

### 5.2 Layer additional modules on top of `starter-default`

```xml
<!-- Add Redis cache -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-redis</artifactId>
</dependency>

<!-- Add distributed lock -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-distributed-lock</artifactId>
</dependency>

<!-- Add distributed ID -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ids-service</artifactId>
</dependency>
```

### 5.3 Exclude Log4j2 and restore Logback

If your business prefers Spring Boot's default Logback:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <exclusions>
        <exclusion>
            <groupId>io.github.dongfangding</groupId>
            <artifactId>ddf-common-log4j</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

And restore the default logging starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>
```

### 5.4 Customize thread-pool metric scanning

`ThreadPoolMetricsBinder` defaults to scanning bean names matching `*Pool*` for `ExecutorService` instances.
To extend matching rules:

```yaml
customizer:
  governance:
    observability:
      thread-pool-patterns:
        - "*Pool*"
        - "*Executor*"
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-starter-web` | The web foundation layer inside `starter-default` |
| `ddf-common-data-mysql-starter` | The data layer inside `starter-default` |
| `ddf-common-governance-starter` | The governance layer inside `starter-default` |
| `ddf-common-redis` | Can be layered on top for Redis cache and distributed lock primitives |
| `ddf-common-distributed-lock` | Can be layered on top for Redisson / Zookeeper distributed locks |
| `ddf-common-authentication` | Can be layered on top for AES token authentication enhancement |

---

## 7. FAQ

**Q1: What's the difference between `starter-default` and `starter-web`?**
- `starter-web` = Web fundamentals (API + Core + MVC + Limit + Log4j2)
- `starter-default` = `starter-web` + MySQL data layer + governance (Mail + Actuator thread-pool metrics)

**Q2: I don't want Druid; how do I switch to HikariCP?**
`ddf-common-data-mysql-starter` imports Druid by default. To use HikariCP:
1. Exclude `druid-spring-boot-3-starter`
2. Configure `spring.datasource.type=com.zaxxer.hikari.HikariDataSource` in `application.yml`

**Q3: Will it fail if I don't configure `spring.mail.*`?**
No. `MailService` is conditionally registered — it only instantiates when both `JavaMailSender` and `MailProperties` beans exist.
Other capabilities are completely unaffected.

**Q4: Can I import both `starter-default` and `starter-web`?**
You can, but it's not recommended because `starter-default` already includes `starter-web`. The duplicate import only adds redundant resolution and won't cause conflicts.

**Q5: How do I see exactly which dependencies this starter pulls in?**
```bash
cd ddf-common-starter-default && mvn dependency:tree
```
Or inspect the dependency tree in your IDE's Maven panel.

---

## 8. References

- Source: `pom.xml`
- Sub-module docs: each sub-module's `CLAUDE.md` and `README.md`
- Example project: `examples/minimal-web-service/`
