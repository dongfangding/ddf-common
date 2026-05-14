# ddf-common-starter-default

> 默认业务场景聚合 starter。将 Web 基础能力、MySQL 数据接入和治理扩展打包为一站式依赖，
> 是常规 Spring Boot 业务应用的推荐首选 starter。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-starter-default` 解决的是 **"常规业务应用一站式基础能力"** 问题。

| 场景             | 典型问题                     | 本 starter 提供的能力 |
|----------------|--------------------------|-----------------|
| 常规单体 / 微服务业务应用 | 每次新建工程都要重复引入 Web、DB、治理依赖 | 一个依赖，全部就绪       |
| CRUD 型后台服务     | 需要统一响应格式 + 数据库 + 邮件告警    | 开箱即用的全套基础能力     |
| 需要快速原型验证       | 不想在依赖配置上花费时间             | 引入即用，专注业务逻辑     |
| 中小型业务团队        | 缺乏基础组件维护人力               | 统一版本、统一行为、统一升级  |

**不适用场景**：

- 纯 Web 网关 / 无数据库的服务 → 使用 `ddf-common-starter-web`
- 使用非 MySQL 数据库（PostgreSQL、Oracle、MongoDB 等）→ 使用 `ddf-common-starter-web` + 自定义数据层
- 只需要 Web + DB 但不需要 Mail / Actuator → 使用 `ddf-common-starter-web` + `ddf-common-data-mysql-starter`

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 版本号由 `ddf-common-dependency` BOM 统一管理，业务工程无需显式声明。

---

## 3. 包含的模块与能力

本 starter 是以下三个 starter 的聚合：

### 3.1 `ddf-common-starter-web`（Web 基础层）

| 子模块                | 核心能力                                                |
|--------------------|-----------------------------------------------------|
| `ddf-common-api`   | 统一响应体 `ResponseData`、业务异常 `BusinessException`、错误码体系 |
| `ddf-common-core`  | 工具集（Hutool/Guava/Fastjson2）、加密、雪花 ID、本地缓存、线程池       |
| `ddf-common-mvc`   | 全局异常处理、响应体自动包装、Jackson 配置、用户上下文 `UserContextUtil`   |
| `ddf-common-limit` | 令牌桶限流 `@RateLimit`、防重复提交 `@Repeatable`              |
| `ddf-common-log4j` | Log4j2 + Disruptor 异步日志（已排除 Logback）                |

### 3.2 `ddf-common-data-mysql-starter`（数据接入层）

| 能力              | 说明                                                    |
|-----------------|-------------------------------------------------------|
| JDBC + MySQL 驱动 | `spring-boot-starter-jdbc` + `mysql-connector-j`      |
| Druid 连接池       | `druid-spring-boot-3-starter`，含 `usePingMethod` 兼容性修复 |
| MyBatis         | `mybatis-spring-boot-starter`                         |

### 3.3 `ddf-common-governance-starter`（治理扩展层）

| 能力      | 说明                                                                           |
|---------|------------------------------------------------------------------------------|
| Mail 服务 | `MailService` / `DefaultMailService` / `MailUtil`，条件注册（需 `spring.mail.*` 配置） |
| 线程池指标   | `ThreadPoolMetricsBinder` 自动扫描线程池 Bean 并绑定到 Micrometer                       |

---

## 4. 最小化配置

### 4.1 数据库配置

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

> `spring.mail.*` 不配置时，`MailService` 不会注册，引入 starter 仍然安全，不会报错。

### 4.2 统一响应格式（开箱即用）

```java
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
        // 实际输出: {"code":200,"message":"成功","data":{"id":1,...}}
    }
}
```

### 4.3 业务异常

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

### 4.4 限流保护

```java
@RateLimit(max = 100, rate = 60)
@GetMapping("/api/orders")
public List<Order> listOrders() {
    return orderService.list();
}
```

### 4.5 防重复提交

```java
@Repeatable(interval = 5000)
@PostMapping("/api/orders")
public Order createOrder(@RequestBody OrderRequest request) {
    return orderService.create(request);
}
```

### 4.6 发送邮件

```java
@Service
public class AlertService {
    @Autowired
    private MailService mailService;

    public void sendAlert(String message) {
        mailService.sendSimpleMail("admin@example.com", "系统告警", message);
    }
}
```

---

## 5. 进阶用法 / 扩展点

### 5.1 Starter 选择决策树

```
是否需要数据库?
├── 否 → ddf-common-starter-web
└── 是 → 是否需要 Mail / Actuator?
    ├── 否 → ddf-common-starter-web + ddf-common-data-mysql-starter
    └── 是 → ddf-common-starter-default （推荐）
```

### 5.2 在 `starter-default` 基础上追加其他模块

```xml
<!-- 追加 Redis 缓存 -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-redis</artifactId>
</dependency>

<!-- 追加分布式锁 -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-distributed-lock</artifactId>
</dependency>

<!-- 追加分布式 ID -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ids-service</artifactId>
</dependency>
```

### 5.3 排除 Log4j2 恢复 Logback

如果业务倾向使用 Spring Boot 默认 Logback：

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

并恢复默认日志 starter：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>
```

### 5.4 自定义线程池指标扫描

`ThreadPoolMetricsBinder` 默认扫描 bean 名称匹配 `*Pool*` 的 `ExecutorService`。如需扩展匹配规则：

```yaml
customizer:
  governance:
    observability:
      thread-pool-patterns:
        - "*Pool*"
        - "*Executor*"
```

---

## 6. 与其他模块协作

| 模块                              | 协作方式                             |
|---------------------------------|----------------------------------|
| `ddf-common-starter-web`        | `starter-default` 的底层 Web 基础层    |
| `ddf-common-data-mysql-starter` | `starter-default` 的数据层           |
| `ddf-common-governance-starter` | `starter-default` 的治理层           |
| `ddf-common-redis`              | 可叠加，提供 Redis 缓存与分布式锁基础           |
| `ddf-common-distributed-lock`   | 可叠加，提供 Redisson / Zookeeper 分布式锁 |
| `ddf-common-authentication`     | 可叠加，提供 AES Token 认证增强            |

---

## 7. FAQ

**Q1：`starter-default` 和 `starter-web` 有什么区别？**

- `starter-web` = Web 基础能力（API + Core + MVC + Limit + Log4j2）
- `starter-default` = `starter-web` + MySQL 数据层 + 治理层（Mail + Actuator 线程池指标）

**Q2：不想用 Druid，想换 HikariCP 怎么办？**
`ddf-common-data-mysql-starter` 默认引入 Druid。如需使用 HikariCP：

1. 排除 `druid-spring-boot-3-starter`
2. 在 `application.yml` 中配置 `spring.datasource.type=com.zaxxer.hikari.HikariDataSource`

**Q3：不配置 `spring.mail.*` 会报错吗？**
不会。`MailService` 采用条件注册，只有当 `JavaMailSender` 和 `MailProperties` 都存在时才实例化。不配置邮件时，其他能力完全不受影响。

**Q4：可以同时引入 `starter-default` 和 `starter-web` 吗？**
可以但不推荐，因为 `starter-default` 已经包含 `starter-web`，重复引入只会增加冗余解析，不会导致冲突。

**Q5：如何查看当前 starter 引入了哪些具体依赖？**

```bash
cd ddf-common-starter-default && mvn dependency:tree
```

或在 IDE 的 Maven 面板中查看依赖树。

---

## 8. 参考

- 源码：`pom.xml`
- 子模块文档：各子模块根目录的 `CLAUDE.md` 与 `README.zh-CN.md`
- 示例工程：`examples/minimal-web-service/`
