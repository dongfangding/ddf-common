# CLAUDE.md

## 模块简介

提供接口限流和防重复提交功能，基于 Redis 实现。

## 核心类

| 类路径                                                           | 功能      |
|---------------------------------------------------------------|---------|
| `com.ddf.boot.common.limit.ratelimit.handler.RateLimitAspect` | 限流切面    |
| `com.ddf.boot.common.limit.repeatable.handler.RepeatAspect`   | 防重复提交切面 |
| `com.ddf.boot.common.limit.ratelimit.annotation.RateLimit`    | 限流注解    |
| `com.ddf.boot.common.limit.repeatable.annotation.Repeatable`  | 防重复提交注解 |
| `com.ddf.boot.common.limit.ratelimit.algorithm.RateLimitAlgorithm` | 限流算法策略接口 |
| `com.ddf.boot.common.limit.ratelimit.algorithm.TokenBucketRateLimitAlgorithm` | 默认令牌桶算法 |
| `com.ddf.boot.common.limit.ratelimit.event.RateLimitTriggeredEvent` | 限流触发事件 |

## 使用说明

### 1. 启用限流

```java
@SpringBootApplication
@EnableRateLimit  // 启用限流功能
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 接口限流

```java
// 基础限流
@RateLimit(max = 100, rate = 1)
@GetMapping("/api/test")
public ResponseData<String> test() {
    return ResponseData.success("OK");
}

// 按 IP 限流
@RateLimit(
    max = 50,                             // 最大请求数
    rate = 10,                            // 填充速率
    keyGenerator = "ipRateLimitKeyGenerator"  // 按 IP 限流（bean name）
)
@GetMapping("/api/sensitive")
public ResponseData<String> sensitive() {
    return ResponseData.success("OK");
}

// 忽略限流（白名单）
@RateLimitIgnore
@GetMapping("/api/public")
public ResponseData<String> publicApi() {
    return ResponseData.success("OK");
}
```

### 3. 防重复提交

```java
// 启用防重复提交
@EnableRepeatable

// 在 Controller 方法上使用
@Repeatable(interval = 5, timeUnit = TimeUnit.SECONDS)
@PostMapping("/api/order")
public ResponseData<String> createOrder(@RequestBody OrderRequest request) {
    return ResponseData.success("下单成功");
}

// 忽略防重复提交
@RepeatableIgnore
@PostMapping("/api/query")
public ResponseData<String> query(@RequestBody QueryRequest request) {
    return ResponseData.success("查询成功");
}
```

### 4. 自定义 Key 生成

```java
// 实现 RateLimitKeyGenerator 接口
@Component
public class CustomRateLimitKeyGenerator implements RateLimitKeyGenerator {

    @Override
    public String buildKey(JoinPoint joinPoint, RateLimit rateLimit) {
        // 自定义限流 key 逻辑
        HttpServletRequest request = getRequest();
        return "custom:rate:" + request.getHeader("X-User-Id");
    }
}
```

## 限流算法

| 算法   | 说明        | 适用场景 |
|------|-----------|------|
| 滑动窗口 | 基于时间窗口的计数 | 平滑限流 |
| 令牌桶  | 固定速率生成令牌  | 突发流量 |
| 漏桶   | 以固定速率处理请求 | 平滑处理 |

### 5. 自定义限流算法

默认使用 `TokenBucketRateLimitAlgorithm`（bean name `tokenBucket`）。接入方实现 `RateLimitAlgorithm` 并注册 Bean，通过 `@RateLimit` 的 `algorithm()` 字段指定：

```java
@RateLimit(max = 100, rate = 10, algorithm = "myAlgorithm")
@GetMapping("/api/test")
public ResponseData<String> test() {
    return ResponseData.success("OK");
}
```

```java
@Component("myAlgorithm")
public class MyRateLimitAlgorithm implements RateLimitAlgorithm {

    @Override
    public String getAlgorithm() {
        return "myAlgorithm";
    }

    @Override
    public boolean tryAcquire(String key, int max, int rate) {
        // 自定义限流逻辑，返回 false 表示被限流
        return true;
    }
}
```

### 6. 限流触发事件

限流触发时发布 `RateLimitTriggeredEvent`（key + algorithm），接入方用 `@EventListener` 订阅做告警/降级：

```java
@EventListener
public void onRateLimit(RateLimitTriggeredEvent event) {
    String key = event.getKey();
    String algorithm = event.getAlgorithm();
    // 告警或降级处理
}
```

## 配置说明

限流参数通过 `@RateLimit` 注解的属性配置（`max`/`rate`/`keyGenerator`/`algorithm`/`condition`），
无独立的 YAML 配置前缀。`keyGenerator`、`algorithm` 的取值是策略 Bean 的 bean name（字符串）。

## 注意事项

1. **Redis Key**：使用 `ApplicationNamedKeyGenerator` 生成，带应用名前缀
2. **时间单位**：`rate` 单位为秒
3. **白名单**：通过 `@RateLimitIgnore` 或 `@RepeatableIgnore` 跳过限制
4. **分布式**：基于 Redis 实现，支持分布式环境
