# ddf-common-starter-web

> Aggregated starter for Spring Boot web applications. Bundles API protocols, core utilities, MVC infrastructure,
> rate limiting, and Log4j2 logging into a single dependency — ideal for lightweight web services that don't need
> database or governance extensions.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Starter

`ddf-common-starter-web` solves the **"quick-start lightweight web services"** problem.

| Scenario                               | Typical Problem                                                             | What This Starter Provides                           |
|----------------------------------------|-----------------------------------------------------------------------------|------------------------------------------------------|
| Pure web gateway / BFF layer           | Need request forwarding, uniform response format, global exception handling | API protocols + MVC interceptors + response wrapping |
| Config-center / registry client        | No persistent storage needed, just management endpoints                     | Core utilities + rate limiting protection            |
| File processing / compute microservice | Light business logic, no database dependency                                | Thread pools, encryption, ID generation, local cache |
| Business with independent data layer   | Data layer uses another stack (e.g. MongoDB, PostgreSQL)                    | Web fundamentals, data layer is your choice          |

**Not suitable for**: applications requiring JDBC / MySQL / Druid / MyBatis. Use `ddf-common-starter-default`
or add `ddf-common-data-mysql-starter` separately.

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> Version is managed by the `ddf-common-dependency` BOM; no explicit version needed in business projects.

---

## 3. Included Modules & Capabilities

This starter aggregates the following modules. One dependency gives you all:

| Sub-module         | Core Capability                                                                                                                                            | Auto-configuration             |
|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|
| `ddf-common-api`   | Uniform response `ResponseData`, business exception `BusinessException`, error-code interface `BaseCallbackCode`, predefined enums                         | None (pure protocol, no beans) |
| `ddf-common-core`  | Hutool / Guava / Fastjson2 utilities, Spring context support, AES/MD5/SHA encryption, Snowflake IDs, local cache (Caffeine/Guava/Hutool), thread pools     | `CoreAutoConfiguration`        |
| `ddf-common-mvc`   | Global exception handling, auto response wrapping (`@ResponseBodyAdvice`), Jackson serialization config, user context `UserContextUtil`, login interceptor | `MvcAutoConfiguration`         |
| `ddf-common-limit` | Token-bucket rate limiting (`@RateLimit`), anti-replay (`@Repeatable`), rate-limit key generator extension                                                 | `RateLimitAutoConfiguration`   |
| `ddf-common-log4j` | Log4j2 + Disruptor async logging, excludes Spring Boot default Logback                                                                                     | None (dependency replacement)  |

**Not included**:

- Database access (JDBC / MySQL / Druid / MyBatis)
- Mail service (Mail / JavaMailSender)
- Actuator governance extensions (thread-pool metrics binding, etc.)
- Authentication utilities (`ddf-common-authentication` is a separate module, not part of this starter)

---

## 4. Minimum Configuration

After adding the dependency, the following capabilities work out of the box with zero extra config:

### 4.1 Uniform response format

Controller return values are automatically wrapped as `ResponseData`:

```java
@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        return "world";   // Actual output: {"code":200,"message":"success","data":"world"}
    }
}
```

### 4.2 Global exception handling

`BusinessException` thrown in the business layer is automatically caught and converted to a standard response:

```java
throw new BusinessException(BaseErrorCallbackCode.BAD_REQUEST);
// Output: {"code":400,"message":"Bad request","data":null}
```

### 4.3 Rate limiting

```java
@RateLimit(max = 100, rate = 60)
@GetMapping("/api/users")
public List<User> listUsers() {
    return userService.list();
}
```

### 4.4 Anti-replay

```java
@Repeatable(interval = 5000)
@PostMapping("/api/orders")
public Order createOrder(@RequestBody OrderRequest request) {
    return orderService.create(request);
}
```

### 4.5 User context

In requests that pass the interceptor chain, retrieve the current user via `UserContextUtil`:

```java
Long userId = UserContextUtil.getUserId();
String imei = UserContextUtil.getImei();
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Layer on additional starters

After importing `ddf-common-starter-web`, add other capabilities as needed:

```xml
<!-- Add database support -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
</dependency>

<!-- Add governance (Mail + Actuator) -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
</dependency>
```

### 5.2 Use `ddf-common-starter-default` for everything

If your business needs the full Web + MySQL + governance combo, use `ddf-common-starter-default` directly.
It equals `starter-web` + `data-mysql-starter` + `governance-starter`:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
</dependency>
```

### 5.3 Custom rate-limit key generator

Implement `RateLimitKeyGenerator` and register it as a Spring Bean to replace the default global/identity strategies:

```java
@Component
public class CustomRateLimitKeyGenerator implements RateLimitKeyGenerator {
    @Override
    public String generate(String className, String methodName, Object[] args) {
        return "custom:" + className + ":" + methodName;
    }
}
```

Then reference it in `@RateLimit`:

```java
@RateLimit(keyGenerator = "customRateLimitKeyGenerator", max = 50, rate = 60)
```

### 5.4 Exclude Log4j2 and restore Logback

If you prefer Spring Boot's default Logback, exclude `ddf-common-log4j`:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
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

---

## 6. Interplay with Other Modules

| Module                          | How They Cooperate                                                |
|---------------------------------|-------------------------------------------------------------------|
| `ddf-common-data-mysql-starter` | Adds database capabilities; complements `starter-web`             |
| `ddf-common-governance-starter` | Adds Mail + Actuator governance                                   |
| `ddf-common-starter-default`    | Aggregates `starter-web` + database + governance; the full bundle |
| `ddf-common-redis`              | Import separately for Redis cache / distributed lock              |
| `ddf-common-distributed-lock`   | Import separately for distributed locking                         |

---

## 7. FAQ

**Q1: Should I choose `starter-web` or `starter-default`?**

- Web + utilities + rate limiting only → `starter-web`
- Also need MySQL + Druid + Mail + Actuator → `starter-default`

**Q2: Why isn't `ddf-common-authentication` included in `starter-web`?**
`ddf-common-authentication` provides AES-encrypted tokens and authentication tools, which is an optional security enhancement.
Some businesses use OAuth2 / JWT / other auth schemes, so it remains an independent import and is not forced into the bundle.

**Q3: No logs after importing this starter?**
`ddf-common-log4j` excludes Logback; you must provide a `log4j2.xml` under `src/main/resources`.
See `ddf-common-log4j`'s `log4j2_demo.xml` for a reference configuration.

**Q4: Can I exclude just one sub-module from `starter-web`?**
Yes. For example, if you want web capabilities but not rate limiting:

```xml
<exclusions>
    <exclusion>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-limit</artifactId>
    </exclusion>
</exclusions>
```

However, it's usually cleaner to import the underlying modules you actually need (e.g. `ddf-common-api` + `ddf-common-core` + `ddf-common-mvc`)
rather than excluding heavily from a starter.

**Q5: Does `starter-web` support service registries like Nacos or Eureka?**
This starter does not include registry clients. Add the corresponding Spring Cloud starter in your business project;
it does not conflict with `ddf-common-starter-web`.

---

## 8. References

- Source: `pom.xml`
- Sub-module docs: each sub-module's `CLAUDE.md` and `README.md`
- Spring Boot Starter mechanism: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration
