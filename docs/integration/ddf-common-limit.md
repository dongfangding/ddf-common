# ddf-common-limit 接入指南

> 接口限流与防重复提交：注解驱动、基于 Redis 的分布式限流，支持可插拔的 Key 生成策略与限流算法，限流触发时发布事件。

## 核心能力

| 能力 | 说明 | 关键类 / 入口 |
|------|------|--------------|
| 限流开启 | 注解开启切面与全局属性 | `ratelimit.annotation.EnableRateLimit` |
| 限流注解 | 方法 / 类级限流，支持 SpEL 条件 | `ratelimit.annotation.RateLimit` |
| 多规则限流 | 同一方法多条限流规则 | `ratelimit.annotation.MultiRateLimit` |
| 忽略限流 | 方法级跳过限流 | `ratelimit.annotation.RateLimitIgnore` |
| Key 生成策略 | 控制限流粒度（全局 / 身份 / IP） | `ratelimit.keygenerator.RateLimitKeyGenerator` |
| 限流算法 | 可插拔算法，默认令牌桶 | `ratelimit.algorithm.RateLimitAlgorithm` / `TokenBucketRateLimitAlgorithm` |
| 限流事件 | 触发限流时发布 | `ratelimit.event.RateLimitTriggeredEvent` |
| 防重复提交 | 基于注解的防重 | `repeatable.annotation.Repeatable` / `EnableRepeatable` |

## 接入方式

### 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-limit</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 传递依赖 `ddf-common-mvc`、`ddf-common-authentication`、`ddf-common-redis`。

### 启用限流

限流切面与全局属性由 `@EnableRateLimit`（`@Import(RateLimitRegistrar.class)`）注册，**需在启动类显式开启**；默认令牌桶算法由 `LimitAutoConfiguration` 自动装配：

```java
@SpringBootApplication
@EnableRateLimit(max = 0, rate = 0)   // max/rate 为 0 表示全局不控制，由各接口注解决定
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 接口限流

```java
// 基础限流：该接口最多 100 个令牌，每秒恢复 1 个
@RateLimit(max = 100, rate = 1)
@GetMapping("/api/test")
public ResponseData<String> test() {
    return ResponseData.success("OK");
}

// 按 IP 限流（keyGenerator 传 Bean 名）
@RateLimit(max = 50, rate = 10, keyGenerator = "ipRateLimitKeyGenerator")
@GetMapping("/api/sensitive")
public ResponseData<String> sensitive() {
    return ResponseData.success("OK");
}

// SpEL 条件限流：满足条件才限流
@RateLimit(max = 10, rate = 1, condition = "#userId != null")
@GetMapping("/api/conditional")
public ResponseData<String> conditional(String userId) {
    return ResponseData.success("OK");
}

// 多条限流规则
@MultiRateLimit(rules = {
        @RateLimit(max = 100, rate = 10),
        @RateLimit(max = 10, rate = 1, keyGenerator = "identityRateLimitKeyGenerator")
})
@PostMapping("/api/order")
public ResponseData<String> createOrder() {
    return ResponseData.success("OK");
}

// 忽略限流（白名单）
@RateLimitIgnore
@GetMapping("/api/public")
public ResponseData<String> publicApi() {
    return ResponseData.success("OK");
}
```

> `keyGenerator` / `algorithm` 均为 **Bean 名称字符串**，非 Class 引用。

### 关键配置

本模块限流参数**全部来自注解属性**，不提供 `@ConfigurationProperties` YAML 前缀。全局默认值在 `@EnableRateLimit` 上声明：

| `@EnableRateLimit` 属性 | 默认值 | 说明 |
|------|------|------|
| `keyGenerator` | `globalRateLimitKeyGenerator` | 全局默认 Key 生成器 Bean 名 |
| `cloudRefresh` | `false` | 是否启用动态刷新（配合 `RateLimitPropertiesCollect`） |
| `max` | `0` | 全局默认令牌桶上限，`0` 表示不控制 |
| `rate` | `0` | 全局默认恢复速率（个/秒），`0` 表示不控制 |

## 扩展点

### 1. RateLimitKeyGenerator 自定义 Key 粒度

实现 `RateLimitKeyGenerator` 并注册 Bean，通过 `keyGenerator` 指定。内置三种：`globalRateLimitKeyGenerator`（默认，按类 + 方法）、`identityRateLimitKeyGenerator`（按用户 / 设备）、`ipRateLimitKeyGenerator`（按 IP）。

```java
@Component("userIdRateLimitKeyGenerator")
public class UserIdRateLimitKeyGenerator implements RateLimitKeyGenerator {

    @Override
    public String generateKey(JoinPoint joinPoint, RateLimit annotation, RateLimitProperties properties) {
        // 自定义 key 粒度，返回的字符串即最终限流 key
        return "custom:" + UserContextUtil.getUserId();
    }
}
```

### 2. RateLimitAlgorithm 自定义限流算法

默认 `TokenBucketRateLimitAlgorithm`（Bean 名 / 算法标识 `tokenBucket`）。实现 `RateLimitAlgorithm` 并以 `getAlgorithm()` 返回值作为 Bean 名注册：

```java
@Component("slidingWindow")
public class SlidingWindowRateLimitAlgorithm implements RateLimitAlgorithm {

    @Override
    public String getAlgorithm() {
        return "slidingWindow";
    }

    @Override
    public boolean tryAcquire(String key, int max, int rate) {
        // 返回 false 表示被限流
        return true;
    }
}
```

使用：

```java
@RateLimit(max = 100, rate = 10, algorithm = "slidingWindow")
@GetMapping("/api/test")
public ResponseData<String> test() { ... }
```

### 3. RateLimitTriggeredEvent 限流触发事件

被限流时发布 `RateLimitTriggeredEvent`（携带 key 与 algorithm），可用于告警 / 降级：

```java
@EventListener
public void onRateLimit(RateLimitTriggeredEvent event) {
    log.warn("接口被限流, key={}, algorithm={}", event.getKey(), event.getAlgorithm());
}
```

## 注意事项

1. **注解属性即配置**：无 YAML 前缀；`rate` 单位为「个/秒」，`max`/`rate` 为 `0` 表示不控制（`RateLimitProperties.NOT_CONTROL`）。
2. **Bean 名引用**：`keyGenerator` 与 `algorithm` 均按 Bean 名查找，未注册对应 Bean 会抛 `NoSuchBeanDefinitionException`。
3. **限流失败行为**：触发限流时抛出 `BusinessException(LimitExceptionCode.RATE_LIMIT)`。
4. **分布式**：默认令牌桶基于 Redis（`RedisTemplateHelper`），支持分布式环境；`identity` Key 生成依赖 `UserContextUtil` 中的用户 / 设备标识。
5. **动态刷新**：`cloudRefresh=true` 时需同时实现 `RateLimitPropertiesCollect` 接口提供实时属性，否则启动报错。
