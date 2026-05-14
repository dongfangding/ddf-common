# ddf-common-starter-web

> Web 场景聚合 starter。将 API 协议、核心工具、MVC 基础设施、限流能力和 Log4j2 日志栈打包为一站式依赖，
> 适合不需要数据库和治理扩展的轻量 Web 服务。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-starter-web` 解决的是 **"轻量 Web 服务快速启动"** 问题。

| 场景               | 典型问题                            | 本 starter 提供的能力         |
|------------------|---------------------------------|-------------------------|
| 纯 Web 网关 / BFF 层 | 只需要接口转发、统一响应格式、全局异常处理           | API 协议 + MVC 拦截器 + 响应包装 |
| 配置中心 / 注册中心客户端   | 不需要持久化存储，只需暴露管理端点               | Core 工具 + 限流保护          |
| 文件处理 / 计算型微服务    | 业务逻辑轻，不依赖数据库                    | 线程池、加密、ID 生成、本地缓存       |
| 已有独立数据层的业务       | 数据层用其他技术栈（如 MongoDB、PostgreSQL） | Web 基础能力，数据层自行选择        |

**不适用场景**：需要 JDBC / MySQL / Druid / MyBatis 时，请使用 `ddf-common-starter-default` 或单独引入 `ddf-common-data-mysql-starter`。

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 版本号由 `ddf-common-dependency` BOM 统一管理，业务工程无需显式声明。

---

## 3. 包含的模块与能力

本 starter 是以下模块的聚合，引入一个即可获得全部能力：

| 子模块                | 核心能力                                                                                                 | 自动配置                         |
|--------------------|------------------------------------------------------------------------------------------------------|------------------------------|
| `ddf-common-api`   | 统一响应体 `ResponseData`、业务异常 `BusinessException`、错误码接口 `BaseCallbackCode`、预定义枚举 `BaseErrorCallbackCode` | 无（纯协议，无 Bean）                |
| `ddf-common-core`  | Hutool / Guava / Fastjson2 工具集、Spring 上下文支撑、AES/MD5/SHA 加密、雪花 ID、本地缓存（Caffeine/Guava/Hutool）、线程池     | `CoreAutoConfiguration`      |
| `ddf-common-mvc`   | 全局异常处理、响应体自动包装 (`@ResponseBodyAdvice`)、Jackson 序列化配置、用户上下文 `UserContextUtil`、登录拦截器                   | `MvcAutoConfiguration`       |
| `ddf-common-limit` | 令牌桶限流 (`@RateLimit`)、防重复提交 (`@Repeatable`)、限流 Key 生成器扩展                                              | `RateLimitAutoConfiguration` |
| `ddf-common-log4j` | Log4j2 + Disruptor 异步日志、已排除 Spring Boot 默认 Logback                                                   | 无（依赖替换）                      |

**不包含的能力**：

- 数据库接入（JDBC / MySQL / Druid / MyBatis）
- 邮件服务（Mail / JavaMailSender）
- Actuator 治理扩展（线程池指标绑定等）
- 认证加密工具（`ddf-common-authentication` 为独立模块，非本 starter 的一部分）

---

## 4. 最小化配置

引入依赖后，以下能力开箱即用，无需额外配置：

### 4.1 统一响应格式

Controller 方法返回任意对象，会被自动包装为 `ResponseData`：

```java
@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        return "world";   // 实际输出: {"code":200,"message":"成功","data":"world"}
    }
}
```

### 4.2 全局异常处理

业务层抛出的 `BusinessException` 会被自动捕获并转为标准响应：

```java
throw new BusinessException(BaseErrorCallbackCode.BAD_REQUEST);
// 输出: {"code":400,"message":"请求参数错误","data":null}
```

### 4.3 限流保护

```java
@RateLimit(max = 100, rate = 60)
@GetMapping("/api/users")
public List<User> listUsers() {
    return userService.list();
}
```

### 4.4 防重复提交

```java
@Repeatable(interval = 5000)
@PostMapping("/api/orders")
public Order createOrder(@RequestBody OrderRequest request) {
    return orderService.create(request);
}
```

### 4.5 用户上下文

在拦截器链通过的请求中，通过 `UserContextUtil` 获取当前用户：

```java
Long userId = UserContextUtil.getUserId();
String imei = UserContextUtil.getImei();
```

---

## 5. 进阶用法 / 扩展点

### 5.1 按需叠加其他 starter

引入 `ddf-common-starter-web` 后，可按需追加其他能力：

```xml
<!-- 追加数据库能力 -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
</dependency>

<!-- 追加治理能力（Mail + Actuator） -->
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
</dependency>
```

### 5.2 使用 `ddf-common-starter-default` 一步到位

如果业务需要 Web + MySQL + 治理的完整组合，可直接使用 `ddf-common-starter-default`，它等于 `starter-web` + `data-mysql-starter` + `governance-starter`：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
</dependency>
```

### 5.3 自定义限流 Key 生成器

实现 `RateLimitKeyGenerator` 接口并注册为 Spring Bean，即可替换默认的全局/身份限流 Key 策略：

```java
@Component
public class CustomRateLimitKeyGenerator implements RateLimitKeyGenerator {
    @Override
    public String generate(String className, String methodName, Object[] args) {
        return "custom:" + className + ":" + methodName;
    }
}
```

然后在 `@RateLimit` 中指定：

```java
@RateLimit(keyGenerator = "customRateLimitKeyGenerator", max = 50, rate = 60)
```

### 5.4 排除 Log4j2 恢复 Logback

如果业务更倾向使用 Spring Boot 默认的 Logback，排除 `ddf-common-log4j`：

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

并恢复默认日志 starter：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>
```

---

## 6. 与其他模块协作

| 模块                              | 协作方式                              |
|---------------------------------|-----------------------------------|
| `ddf-common-data-mysql-starter` | 追加数据库能力，与 `starter-web` 互补        |
| `ddf-common-governance-starter` | 追加 Mail + Actuator 治理能力           |
| `ddf-common-starter-default`    | 聚合了 `starter-web` + 数据库 + 治理，是完整版 |
| `ddf-common-redis`              | 如需 Redis 缓存 / 分布式锁，单独引入           |
| `ddf-common-distributed-lock`   | 如需分布式锁，单独引入                       |

---

## 7. FAQ

**Q1：`starter-web` 和 `starter-default` 怎么选？**

- 只需要 Web + 工具 + 限流 → `starter-web`
- 还需要 MySQL + Druid + Mail + Actuator → `starter-default`

**Q2：为什么 `starter-web` 没有包含 `ddf-common-authentication`？**
`ddf-common-authentication` 提供 AES 加密 Token 和认证工具，属于可选安全增强。部分业务可能使用 OAuth2 / JWT 等其他认证方案，因此保持独立引入，不强制打包。

**Q3：引入后没有日志输出？**
`ddf-common-log4j` 已排除 Logback，需要在 `src/main/resources` 下提供 `log4j2.xml` 配置文件。参考 `ddf-common-log4j` 模块的 `log4j2_demo.xml`。

**Q4：可以在 `starter-web` 基础上只排除某个子模块吗？**
可以。例如只需要 Web 能力但不需要限流：

```xml
<exclusions>
    <exclusion>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-limit</artifactId>
    </exclusion>
</exclusions>
```

但通常建议直接按需引入底层模块（如只引 `ddf-common-api` + `ddf-common-core` + `ddf-common-mvc`），而非在 starter 里做大量排除。

**Q5：`starter-web` 是否支持微服务注册中心（如 Nacos / Eureka）？**
本 starter 不包含注册中心客户端。如需接入，在业务工程中额外引入对应 Spring Cloud starter 即可，与 `ddf-common-starter-web` 无冲突。

---

## 8. 参考

- 源码：`pom.xml`
- 子模块文档：各子模块根目录的 `CLAUDE.md` 与 `README.zh-CN.md`
- Spring Boot Starter 机制：https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration
