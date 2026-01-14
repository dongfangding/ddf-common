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
// 基础限流：每分钟最多 100 次
@RateLimit(max = 100, rate = 1, rateInterval = 60)
@GetMapping("/api/test")
public ResponseData<String> test() {
    return ResponseData.success("OK");
}

// 滑动窗口限流
@RateLimit(
    max = 50,                    // 最大请求数
    rate = 10,                   // 填充速率
    rateInterval = 1,            // 速率计算窗口（秒）
    keyGenerator = IpRateLimitKeyGenerator.class  // 按 IP 限流
)
@GetMapping("/api/sensitive")
public ResponseData<String> sensitive() {
    return ResponseData.success("OK");
}

// 忽略限流（白名单）
@RateLimit(ignore = true)
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

## 配置说明

```yaml
ddf:
  rate-limit:
    enabled: true                          # 是否启用
    global-key-prefix: "ddf:rate:"         # 全局限流 key 前缀
    default-max: 100                       # 默认最大请求数
    default-rate-interval-seconds: 60      # 默认时间窗口
```

## 注意事项

1. **Redis Key**：使用 `ApplicationNamedKeyGenerator` 生成，带应用名前缀
2. **时间单位**：`interval` 和 `rateInterval` 默认单位为秒
3. **白名单**：通过 `@RateLimit(ignore = true)` 或 `@RepeatableIgnore` 跳过限制
4. **分布式**：基于 Redis 实现，支持分布式环境
