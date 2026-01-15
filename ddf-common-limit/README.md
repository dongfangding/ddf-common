# ddf-common-limit

限流模块，提供分布式限流和防重复提交功能。

## 功能特性

- 接口限流
- 防重复提交
- 自定义限流策略

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-limit</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径               | 功能   |
|-------------------|------|
| `RateLimitAspect` | 限流切面 |
| `LimitService`    | 限流服务 |
| `LimitProperties` | 配置属性 |

## 使用说明

### 注解方式

```java
@RateLimit(key = "api:user", count = 10, period = 60)
@PostMapping("/user")
public ResponseData<User> createUser(@RequestBody User user) {
    // 限流控制
}
```

### 防重复提交

```java
@NonRepeatableSubmit
@PostMapping("/order")
public ResponseData<Order> createOrder(@RequestBody Order order) {
    // 防止重复提交
}
```
