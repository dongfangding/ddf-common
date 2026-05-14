# ddf-common-limit

> Rate limiting and repeat-submission protection module. Distributed rate limiting via Redis token bucket,
> single-node repeat-submit validation via local cache.
> It is part of `ddf-common-starter-web`; **application services normally pull it in transitively through the starter**.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-limit` answers the cross-cutting question **"how do I govern API traffic and form safety?"**

| Category                     | Typical Problem                                            | What the Module Provides                                                      |
|------------------------------|------------------------------------------------------------|-------------------------------------------------------------------------------|
| API anti-scraping            | Login / SMS endpoints bombarded by high-frequency calls    | `@RateLimit` token-bucket limiting with global / per-user / custom dimensions |
| Multi-level limiting         | Same endpoint needs both total-traffic and per-user caps   | `@MultiRateLimit` stacks multiple rules as layered gates                      |
| Conditional limiting         | Limiting should trigger only for specific parameter values | `@RateLimit(condition = "#type == 'GUEST'")` SpEL expression                  |
| Repeat-submission protection | Double-click creates duplicate orders                      | `@Repeatable` request-parameter fingerprint deduplication                     |
| Load-test toggle             | Global limiting rules skew load-test results               | `@EnableRateLimit` / `@EnableRepeatable` global on/off switch                 |

> ⚠️ The default repeat-submit implementation (`LocalRepeatableValidator`) is **local-cache based** and does
> not support distributed clusters. For multi-instance deployments, implement `RepeatableValidator` with Redis.

---

## 2. Maven Dependency

Application services should **not** depend on this module directly; use the starter:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

`ddf-common-limit` itself depends on `ddf-common-authentication` + `ddf-common-redis` + `ddf-common-mvc`.
Rate-limit keys are generated via `ApplicationNamedKeyGenerator` (prefixed with application name);
the token-bucket algorithm is provided by `ddf-common-redis`.

---

## 3. Minimum Configuration

### 3.1 Enable rate limiting

Add `@EnableRateLimit` on the main class or any `@Configuration`:

```java
@SpringBootApplication
@EnableRateLimit   // Enable rate limiting
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`@EnableRateLimit` attributes (global defaults):

| Attribute      | Description                                                         | Default                       |
|----------------|---------------------------------------------------------------------|-------------------------------|
| `max`          | Token bucket capacity; `0` means unlimited                          | `0`                           |
| `rate`         | Token refill rate (tokens/second); `0` means unlimited              | `0`                           |
| `keyGenerator` | Rate-limit key generator bean name                                  | `globalRateLimitKeyGenerator` |
| `cloudRefresh` | Enable dynamic refresh (requires `RateLimitPropertiesCollect` impl) | `false`                       |

### 3.2 Enable repeat-submission protection

```java
@SpringBootApplication
@EnableRepeatable   // Enable repeat-submit protection
public class Application {
    public static void main(String[] args) { ... }
}
```

`@EnableRepeatable` attributes:

| Attribute         | Description                                                 | Default                    |
|-------------------|-------------------------------------------------------------|----------------------------|
| `interval`        | Interval for treating requests as duplicates (milliseconds) | `1000`                     |
| `globalValidator` | Global validator bean name                                  | `localRepeatableValidator` |

> The two annotations are independent; enable either or both as needed.

---

## 4. Core API Guide

### 4.1 Rate limiting

#### Basic usage

```java
@RestController
public class SmsController {

    // Max 100 requests per minute (bucket capacity 100, refill ~1.67 tokens/sec)
    @RateLimit(max = 100, rate = 1)
    @PostMapping("/api/sms/send")
    public ResponseData<Void> send(@RequestBody SmsRequest request) {
        return ResponseData.success();
    }
}
```

#### Multi-rule stacking

```java
// Global cap 1000 QPS + per-user cap 10 QPS
@MultiRateLimit(rules = {
    @RateLimit(max = 1000, rate = 1000, keyGenerator = "globalRateLimitKeyGenerator"),
    @RateLimit(max = 10, rate = 10, keyGenerator = "identityRateLimitKeyGenerator")
})
@GetMapping("/api/search")
public ResponseData<List<Item>> search(@RequestParam String keyword) {
    return ResponseData.success(itemService.search(keyword));
}
```

#### Conditional limiting

```java
// Only limit when request param type equals "GUEST"
@RateLimit(max = 10, rate = 1, condition = "#type == 'GUEST'")
@GetMapping("/api/resource")
public ResponseData<Resource> getResource(@RequestParam String type) {
    return ResponseData.success(resourceService.get(type));
}
```

#### Ignore limiting

```java
// Class-level统一开启限流
@RateLimit(max = 100, rate = 10)
@RestController
public class OrderController {

    // This endpoint skips limiting
    @RateLimitIgnore
    @GetMapping("/api/order/health")
    public ResponseData<String> health() {
        return ResponseData.success("ok");
    }
}
```

### 4.2 Repeat-submission protection

```java
@RestController
public class OrderController {

    // Same user + same parameters within 5 seconds are treated as duplicate
    @Repeatable(interval = 5000)
    @PostMapping("/api/order")
    public ResponseData<OrderVO> create(@RequestBody CreateOrderRequest request) {
        return ResponseData.success(orderService.create(request));
    }

    // Skip repeat protection (e.g. query endpoints)
    @RepeatableIgnore
    @PostMapping("/api/order/query")
    public ResponseData<OrderVO> query(@RequestBody QueryOrderRequest request) {
        return ResponseData.success(orderService.query(request));
    }
}
```

`@Repeatable` attributes:

| Attribute    | Description                                                              | Default |
|--------------|--------------------------------------------------------------------------|---------|
| `interval`   | Interval in ms; `0` means inherit from `@EnableRepeatable`               | `0`     |
| `validator`  | Validator bean name; empty string means inherit global                   | `""`    |
| `throwError` | Whether to throw exception on duplicate; `false` silently passes through | `true`  |

> `throwError = false` use case: when the original request response may be slower than the duplicate,
> the frontend could receive an error and render a failure page. Setting `false` lets the duplicate
> proceed to the business layer (relying on business-level idempotency).

### 4.3 Rate-limit key generators

Two built-in generators control the limiting dimension:

| Generator           | Bean name                       | Dimension                      | Use case                        |
|---------------------|---------------------------------|--------------------------------|---------------------------------|
| Global method-level | `globalRateLimitKeyGenerator`   | Class name + Method name       | Total interface traffic control |
| Identity-level      | `identityRateLimitKeyGenerator` | userId / imei + Class + Method | Per-user frequency control      |

Custom key generator:

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

Usage:

```java
@RateLimit(max = 50, rate = 5, keyGenerator = "ipRateLimitKeyGenerator")
@GetMapping("/api/public/list")
public ResponseData<List<Item>> list() { ... }
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Dynamic rate-limit refresh (Spring Cloud)

When using a config center (Nacos / Apollo) for hot refresh of `max` / `rate`:

1. Define a `@RefreshScope` config class in your application to receive latest values
2. Implement `RateLimitPropertiesCollect`:

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

3. Enable with `@EnableRateLimit(cloudRefresh = true)`

### 5.2 Custom repeat-submit validator

Implement distributed repeat protection with Redis:

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

Enable:

```java
@EnableRepeatable(globalValidator = RedisRepeatableValidator.BEAN_NAME)
```

Or override per endpoint:

```java
@Repeatable(validator = RedisRepeatableValidator.BEAN_NAME, interval = 3000)
```

### 5.3 Global defaults + local override strategy

`@EnableRateLimit` defines global fallback values; `@RateLimit` inherits unspecified attributes:

```java
@EnableRateLimit(max = 100, rate = 10)   // Global defaults

@RestController
public class ApiController {

    // Inherit global: max=100, rate=10
    @RateLimit
    @GetMapping("/api/a")
    public ResponseData<String> a() { ... }

    // Local override: max=10, rate=1
    @RateLimit(max = 10, rate = 1)
    @GetMapping("/api/b")
    public ResponseData<String> b() { ... }
}
```

---

## 6. Interplay with Other Modules

| Module                      | How They Cooperate                                                                                                          |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `ddf-common-authentication` | `IdentityRateLimitKeyGenerator` reads `UserContextUtil.getUserId()` / `getImei()` for identity keys                         |
| `ddf-common-redis`          | Token-bucket algorithm via `RedisTemplateHelper.tokenBucketRateLimitAcquire`; key prefix via `ApplicationNamedKeyGenerator` |
| `ddf-common-mvc`            | Limit / repeat exceptions caught by the global exception handler and wrapped into `ResponseData`                            |
| `ddf-common-api`            | `LimitExceptionCode.RATE_LIMIT` and `LimitExceptionCode.REPEAT_SUBMIT` implement `BaseCallbackCode`                         |
| `ddf-common-starter-web`    | This module is a core capability of that starter                                                                            |

---

## 7. FAQ

**Q1: What rate-limiting algorithm is used?**  
Token Bucket. Redis stores the current token count in a Hash; each request attempts to acquire one token.
Failure triggers rate limiting. Tokens refill at `rate` (tokens/second) with a bucket capacity of `max`.

**Q2: What does `max = 0` or `rate = 0` mean?**  
`0` is the special value `RateLimitProperties.NOT_CONTROL`, meaning **unlimited**. Used to disable
limiting globally or temporarily deactivate a rule.

**Q3: Why can `@RateLimit` be placed on a class?**  
Class-level `@RateLimit` applies to all public methods of that class. Individual methods can override
with their own `@RateLimit`, or use `@RateLimitIgnore` to skip.

**Q4: Repeat protection fails in a cluster — why?**  
The default `LocalRepeatableValidator` uses Hutool `TimedCache` (local weak-reference cache) and only
works within a single JVM. For multi-instance deployments, implement a Redis-based `RepeatableValidator`
as shown in section 5.2.

**Q5: What variables are available in `condition` SpEL expressions?**  
Method parameter names are exposed as variables. For a signature like `search(String type, Long categoryId)`,
the expression can be `"#type == 'GUEST' && #categoryId > 100"`.

**Q6: How do I quickly disable all limiting and repeat protection for load testing?**  
Remove `@EnableRateLimit` and `@EnableRepeatable` from the main class. Since these annotations register
the Aspects, removing them disables all `@RateLimit` / `@Repeatable` processing (no exceptions thrown;
annotations simply become no-ops).

**Q7: What error is returned when rate limiting triggers?**  
`BusinessException(LimitExceptionCode.RATE_LIMIT)` is thrown. By default the frontend receives
`{ "code": "rate_limit", "message": "接口已限流" }`. Customize the message in your global exception handler.

---

## 8. References

- Source: `ratelimit/annotation/RateLimit.java`, `ratelimit/annotation/MultiRateLimit.java`, `ratelimit/annotation/RateLimitIgnore.java`
- Source: `ratelimit/handler/RateLimitAspect.java`
- Source: `ratelimit/keygenerator/RateLimitKeyGenerator.java`, `ratelimit/keygenerator/GlobalRateLimitKeyGenerator.java`, `ratelimit/keygenerator/IdentityRateLimitKeyGenerator.java`
- Source: `ratelimit/config/RateLimitProperties.java`, `ratelimit/extra/RateLimitPropertiesCollect.java`
- Source: `repeatable/annotation/Repeatable.java`, `repeatable/annotation/RepeatableIgnore.java`, `repeatable/annotation/EnableRepeatable.java`
- Source: `repeatable/handler/RepeatAspect.java`, `repeatable/validator/RepeatableValidator.java`, `repeatable/validator/LocalRepeatableValidator.java`
- Source: `exception/LimitExceptionCode.java`
