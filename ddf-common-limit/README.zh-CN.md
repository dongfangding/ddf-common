# ddf-common-limit

> 接口限流与防重复提交模块。基于 Redis 令牌桶实现分布式限流，基于本地缓存实现单机防重提交校验。
> 它是 `ddf-common-starter-web` 的组成部分，**业务工程一般通过 starter 间接引入**。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-limit` 解决的是 **"接口流量治理与表单安全"** 这一横切问题。

| 场景    | 典型问题                  | 模块提供的能力                                               |
|-------|-----------------------|-------------------------------------------------------|
| 接口防刷  | 登录、短信验证码接口被高频调用       | `@RateLimit` 令牌桶限流，支持全局 / 用户级 / 自定义维度                 |
| 多级限流  | 同一接口既要防总流量击穿，又要控单用户频率 | `@MultiRateLimit` 多规则叠加，一次校验多道闸门                      |
| 条件限流  | 只有特定参数值才触发限流          | `@RateLimit(condition = "#type == 'VIP'")` SpEL 条件表达式 |
| 防重复提交 | 用户快速双击导致订单重复创建        | `@Repeatable` 基于请求参数指纹的防重校验                           |
| 压测开关  | 全局限流规则影响压测结果          | `@EnableRateLimit` / `@EnableRepeatable` 全局一键开关       |

> ⚠️ 防重复提交的默认实现（`LocalRepeatableValidator`）基于**本地缓存**，不支持分布式集群场景。若业务部署多实例，需自行实现 `RepeatableValidator` 基于 Redis 做分布式防重。

---

## 2. 依赖引入

业务工程**不建议直接依赖**，请使用 starter：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

`ddf-common-limit` 本身依赖 `ddf-common-authentication` + `ddf-common-redis` + `ddf-common-mvc`。
限流Key默认使用 `ApplicationNamedKeyGenerator` 生成（带应用名前缀），Redis 令牌桶算法由 `ddf-common-redis` 提供。

---

## 3. 最小化配置

### 3.1 启用限流

在启动类或任意 `@Configuration` 上添加 `@EnableRateLimit`：

```java
@SpringBootApplication
@EnableRateLimit   // 启用限流能力
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`@EnableRateLimit` 属性（全局默认值）：

| 属性             | 说明                                           | 默认值                           |
|----------------|----------------------------------------------|-------------------------------|
| `max`          | 令牌桶最大容量，`0` 表示不控制                            | `0`                           |
| `rate`         | 令牌恢复速率（个/秒），`0` 表示不控制                        | `0`                           |
| `keyGenerator` | 限流 Key 生成器 Bean 名称                           | `globalRateLimitKeyGenerator` |
| `cloudRefresh` | 是否开启动态刷新（需外部实现 `RateLimitPropertiesCollect`） | `false`                       |

### 3.2 启用防重复提交

```java
@SpringBootApplication
@EnableRepeatable   // 启用防重复提交
public class Application {
    public static void main(String[] args) { ... }
}
```

`@EnableRepeatable` 属性：

| 属性                | 说明             | 默认值                        |
|-------------------|----------------|----------------------------|
| `interval`        | 同一次请求的间隔时间（毫秒） | `1000`                     |
| `globalValidator` | 全局校验器 Bean 名称  | `localRepeatableValidator` |

> 两个注解互相独立，可按需只启用其一。

---

## 4. 核心 API 使用指南

### 4.1 接口限流

#### 基础用法

```java
@RestController
public class SmsController {

    // 每分钟最多允许 100 次请求（令牌桶容量 100，每秒恢复 1.67 个 ≈ 100/60）
    @RateLimit(max = 100, rate = 1)
    @PostMapping("/api/sms/send")
    public ResponseData<Void> send(@RequestBody SmsRequest request) {
        return ResponseData.success();
    }
}
```

#### 多规则叠加

```java
// 接口全局控 1000 QPS，同时单用户限 10 QPS
@MultiRateLimit(rules = {
    @RateLimit(max = 1000, rate = 1000, keyGenerator = "globalRateLimitKeyGenerator"),
    @RateLimit(max = 10, rate = 10, keyGenerator = "identityRateLimitKeyGenerator")
})
@GetMapping("/api/search")
public ResponseData<List<Item>> search(@RequestParam String keyword) {
    return ResponseData.success(itemService.search(keyword));
}
```

#### 条件限流

```java
// 仅当请求参数 type 等于 "GUEST" 时才限流
@RateLimit(max = 10, rate = 1, condition = "#type == 'GUEST'")
@GetMapping("/api/resource")
public ResponseData<Resource> getResource(@RequestParam String type) {
    return ResponseData.success(resourceService.get(type));
}
```

#### 忽略限流

```java
// 类上统一开启限流
@RateLimit(max = 100, rate = 10)
@RestController
public class OrderController {

    // 该接口跳过限流
    @RateLimitIgnore
    @GetMapping("/api/order/health")
    public ResponseData<String> health() {
        return ResponseData.success("ok");
    }
}
```

### 4.2 防重复提交

```java
@RestController
public class OrderController {

    // 5 秒内同一用户、同一参数视为重复提交
    @Repeatable(interval = 5000)
    @PostMapping("/api/order")
    public ResponseData<OrderVO> create(@RequestBody CreateOrderRequest request) {
        return ResponseData.success(orderService.create(request));
    }

    // 忽略防重（如查询类接口）
    @RepeatableIgnore
    @PostMapping("/api/order/query")
    public ResponseData<OrderVO> query(@RequestBody QueryOrderRequest request) {
        return ResponseData.success(orderService.query(request));
    }
}
```

`@Repeatable` 属性：

| 属性           | 说明                                        | 默认值    |
|--------------|-------------------------------------------|--------|
| `interval`   | 间隔时间（毫秒），`0` 表示使用 `@EnableRepeatable` 全局值 | `0`    |
| `validator`  | 校验器 Bean 名称，空串表示使用全局值                     | `""`   |
| `throwError` | 检测到重复时是否抛异常；若为 `false` 则静默放行              | `true` |

> `throwError = false` 的适用场景：重复请求到达时，若正常请求的响应可能慢于重复请求，前端收到异常会导致页面错误。设为 `false` 可让重复请求也走到业务层（由业务幂等兜底）。

### 4.3 限流 Key 生成器

模块内置两种 Key 生成器，决定限流维度：

| 生成器   | Bean 名称                         | 维度                       | 适用场景    |
|-------|---------------------------------|--------------------------|---------|
| 全局方法级 | `globalRateLimitKeyGenerator`   | 类名 + 方法名                 | 接口总流量控制 |
| 身份级别  | `identityRateLimitKeyGenerator` | userId / imei + 类名 + 方法名 | 单用户频率控制 |

自定义 Key 生成器：

```java
@Component
public class IpRateLimitKeyGenerator implements RateLimitKeyGenerator {

    public static final String BEAN_NAME = "ipRateLimitKeyGenerator";

    @Override
    public String generateKey(JoinPoint joinPoint, RateLimit annotation, RateLimitProperties properties) {
        String ip = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getRemoteAddr();
        return ApplicationNamedKeyGenerator.genKey(getPrefix(), ip,
                AopUtil.getJoinPointClass(joinPoint).getName(),
                AopUtil.getJoinPointMethod(joinPoint).getName());
    }
}
```

使用自定义生成器：

```java
@RateLimit(max = 50, rate = 5, keyGenerator = "ipRateLimitKeyGenerator")
@GetMapping("/api/public/list")
public ResponseData<List<Item>> list() { ... }
```

---

## 5. 进阶用法 / 扩展点

### 5.1 动态刷新限流参数（Spring Cloud 环境）

当配置中心（Nacos / Apollo）需要热刷新 `max` / `rate` 时：

1. 业务层定义 `@RefreshScope` 配置类接收最新值
2. 实现 `RateLimitPropertiesCollect` 接口：

```java
@Component
public class DynamicRateLimitProperties implements RateLimitPropertiesCollect {

    @Autowired private RateLimitDynamicConfig config;  // @RefreshScope

    @Override
    public void copyToProperties(RateLimitProperties properties) {
        properties.setMax(config.getMax());
        properties.setRate(config.getRate());
    }
}
```

3. `@EnableRateLimit(cloudRefresh = true)` 开启实时刷新

### 5.2 自定义防重复提交校验器

实现分布式防重（基于 Redis）：

```java
@Component
public class RedisRepeatableValidator implements RepeatableValidator {

    public static final String BEAN_NAME = "redisRepeatableValidator";

    @Autowired private StringRedisTemplate redisTemplate;

    @Override
    public boolean check(JoinPoint joinPoint, Repeatable repeatable,
            String currentUid, RepeatableProperties properties) {
        long interval = repeatable.interval() == 0 ? properties.getInterval() : repeatable.interval();
        String key = "repeat:" + currentUid + ":"
                + AopUtil.getJoinPointClass(joinPoint).getName() + ":"
                + AopUtil.getJoinPointMethod(joinPoint).getName() + ":"
                + AopUtil.serializeParam(joinPoint);
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, "1", interval, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(set);
    }
}
```

启用：

```java
@EnableRepeatable(globalValidator = RedisRepeatableValidator.BEAN_NAME)
```

或单接口覆盖：

```java
@Repeatable(validator = RedisRepeatableValidator.BEAN_NAME, interval = 3000)
```

### 5.3 全局默认 + 局部覆盖策略

`@EnableRateLimit` 定义全局兜底值，`@RateLimit` 未显式指定的属性继承全局：

```java
@EnableRateLimit(max = 100, rate = 10)   // 全局默认

@RestController
public class ApiController {

    // 继承全局：max=100, rate=10
    @RateLimit
    @GetMapping("/api/a")
    public ResponseData<String> a() { ... }

    // 局部覆盖：max=10, rate=1
    @RateLimit(max = 10, rate = 1)
    @GetMapping("/api/b")
    public ResponseData<String> b() { ... }
}
```

---

## 6. 与其他模块协作

| 模块                          | 协作方式                                                                                                   |
|-----------------------------|--------------------------------------------------------------------------------------------------------|
| `ddf-common-authentication` | `IdentityRateLimitKeyGenerator` 通过 `UserContextUtil.getUserId()` / `getImei()` 获取身份标识                  |
| `ddf-common-redis`          | 令牌桶算法由 `RedisTemplateHelper.tokenBucketRateLimitAcquire` 提供；Key 前缀通过 `ApplicationNamedKeyGenerator` 拼接 |
| `ddf-common-mvc`            | 限流异常 / 防重复异常由全局异常处理器统一捕获并包装为 `ResponseData`                                                            |
| `ddf-common-api`            | 异常码 `LimitExceptionCode.RATE_LIMIT`、`LimitExceptionCode.REPEAT_SUBMIT` 实现 `BaseCallbackCode`           |
| `ddf-common-starter-web`    | 本模块是 starter 的核心能力组成部分                                                                                 |

---

## 7. FAQ

**Q1：限流算法是哪种？**  
令牌桶（Token Bucket）。Redis 中以 Hash 结构维护当前令牌数，每次请求尝试获取一个令牌；获取失败即触发限流。令牌按 `rate`（个/秒）匀速恢复，桶容量为 `max`。

**Q2：`max = 0` 或 `rate = 0` 是什么意思？**  
`0` 是特殊值 `RateLimitProperties.NOT_CONTROL`，表示**不控制**。用于全局默认值关闭限流，或某条规则临时停用。

**Q3：为什么 `@RateLimit` 可以放在类上？**  
类级 `@RateLimit` 会为该类所有 public 方法统一应用限流规则；若个别方法需要特殊处理，可用 `@RateLimit` 覆盖，或用 `@RateLimitIgnore` 跳过。

**Q4：防重复提交在集群环境下失效怎么办？**  
默认 `LocalRepeatableValidator` 基于 Hutool `TimedCache`（本地弱引用缓存），仅作用于单 JVM。多实例部署时，请按「5.2 自定义防重复提交校验器」实现基于 Redis 的分布式版本。

**Q5：`condition` SpEL 表达式可以访问哪些变量？**  
方法参数名作为变量名直接访问。例如方法签名为 `search(String type, Long categoryId)`，则表达式可写为 `"#type == 'GUEST' && #categoryId > 100"`。

**Q6：压测时如何快速关闭所有限流和防重？**  
移除启动类上的 `@EnableRateLimit` 和 `@EnableRepeatable` 即可。由于这两个注解负责注册 Aspect，移除后所有 `@RateLimit` / `@Repeatable` 将失效（不会抛异常，只是不执行逻辑）。

**Q7：限流触发后返回什么错误？**  
抛出 `BusinessException(LimitExceptionCode.RATE_LIMIT)`，默认前端收到 `{ "code": "rate_limit", "message": "接口已限流" }`。可在全局异常处理器中自定义该错误码的响应文案。

---

## 8. 参考

- 源码：`ratelimit/annotation/RateLimit.java`、`ratelimit/annotation/MultiRateLimit.java`、`ratelimit/annotation/RateLimitIgnore.java`
- 源码：`ratelimit/handler/RateLimitAspect.java`
- 源码：`ratelimit/keygenerator/RateLimitKeyGenerator.java`、`ratelimit/keygenerator/GlobalRateLimitKeyGenerator.java`、`ratelimit/keygenerator/IdentityRateLimitKeyGenerator.java`
- 源码：`ratelimit/config/RateLimitProperties.java`、`ratelimit/extra/RateLimitPropertiesCollect.java`
- 源码：`repeatable/annotation/Repeatable.java`、`repeatable/annotation/RepeatableIgnore.java`、`repeatable/annotation/EnableRepeatable.java`
- 源码：`repeatable/handler/RepeatAspect.java`、`repeatable/validator/RepeatableValidator.java`、`repeatable/validator/LocalRepeatableValidator.java`
- 源码：`exception/LimitExceptionCode.java`
