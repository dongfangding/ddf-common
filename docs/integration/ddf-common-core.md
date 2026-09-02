# ddf-common-core 接入指南

> 轻量公共能力内核：加密、ID 生成、线程池、Spring 支撑、Token 策略与认证事件，是其他所有模块的基础依赖。

## 核心能力

| 能力           | 说明                             | 关键类 / 入口                                                 |
|--------------|--------------------------------|----------------------------------------------------------|
| 加密工具         | AES / RSA / BCrypt 加密、解密、密码散列  | `com.ddf.boot.common.core.util.SecureUtil`               |
| ID 生成        | 雪花算法（可配 workerId/dataCenterId） | `com.ddf.boot.common.core.util.IdsUtil`                  |
| 线程池构建        | 统一构造、优雅关闭、运行状态监控               | `com.ddf.boot.common.core.helper.ThreadBuilderHelper`    |
| Spring 上下文持有 | 静态获取 Bean / ApplicationContext | `com.ddf.boot.common.core.helper.SpringContextHolder`    |
| 全局配置属性       | 雪花参数、RSA/AES/签名密钥等             | `com.ddf.boot.common.core.config.GlobalProperties`       |
| Token 生成策略   | Token 生成/解析/校验/刷新的策略接口         | `com.ddf.boot.common.core.authentication.TokenGenerator` |
| 认证事件         | 登录成功 / Token 刷新 / 登录失败事件       | `com.ddf.boot.common.core.event.*`                       |

## 接入方式

### 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-core</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> `${ddf-common.version}` 当前为 `boot3.5-2026.1-SNAPSHOT`，由父工程的 `<revision>` 统一管理。

核心模块通过 `CoreAutoConfiguration` 自动装配，无需额外开关。默认注册 `EnvironmentHelper`、`DeferredHelper`、`CompletableFutureHelper`、线程池优雅关闭定义，以及 `TokenGenerator`（见下文扩展点）。

### 关键配置

配置前缀 `customizer.infra.global-properties`（对应 `GlobalProperties`）：

```yaml
customizer:
  infra:
    global-properties:
      snowflake-worker-id: 1          # 雪花算法 workerId（0~31），多实例需唯一
      snowflake-data-center-id: 1     # 雪花算法 dataCenterId（0~31），多实例需唯一
      rsa-private-key: "..."          # RSA 私钥
      rsa-public-key: "..."           # RSA 公钥
      aes-secret: "32字节密钥"         # AES 密钥（16/24/32 字节）
      sign-secret: "..."              # HMAC256 签名密钥（建议 32 字节）
      exception-code-to-response-status: false  # 异常码是否同步为 HTTP 状态码
      global-log-print-details: false          # 是否打印全局日志详情
```

### 常用工具示例

```java
// AES
String enc = SecureUtil.aesEncryptHex("明文");
String dec = SecureUtil.aesDecryptStr(enc);

// RSA
String rsa = SecureUtil.rsaPublicEncryptHex(data);
String origin = SecureUtil.rsaPrivateDecryptStr(rsa);

// BCrypt
String encoded = SecureUtil.bCryptEncoder("password");
boolean ok = SecureUtil.bCryptMatch("password", encoded);

// ID
long id = IdsUtil.getNextLongId();
String strId = IdsUtil.getNextStrId();
String uniqueId = IdsUtil.getUniqueId();

// 线程池（自动优雅关闭 + 运行状态监控）
ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
        "my-pool", 60, 100); // 前缀 / keepAlive 秒 / 队列容量

// 静态取 Bean
SomeService service = SpringContextHolder.getBean(SomeService.class);
```

## 扩展点

### 1. TokenGenerator 策略

默认注册 `DefaultTokenGenerator`（AES 加密 Token 内容，若存在 `TokenCache` 则联动缓存），通过 `@ConditionalOnMissingBean(TokenGenerator.class)` 生效——接入方只要提供一个 `TokenGenerator` 类型的 Bean 即可整体替换：

```java
@Component
public class MyTokenGenerator implements TokenGenerator {

    @Override
    public AuthenticateToken createToken(UserClaim userClaim) {
        // 生成 token，返回 AuthenticateToken
    }

    @Override
    public UserClaim getUserClaim(String token) {
        // 从 token 解析用户信息
    }

    @Override
    public AuthenticateCheckResult checkToken(String token) {
        // 校验 token，返回校验结果
    }

    @Override
    public void refreshToken(String userId, String token) {
        // 刷新 token
    }
}
```

> 旧的 `TokenUtil` 已 `@Deprecated`，内部委托给 `TokenGenerator`，新代码请直接注入 `TokenGenerator`。

### 2. TokenCache（可选）

`TokenGenerator` 不强制依赖缓存。提供 `TokenCache` 实现即可让默认 `DefaultTokenGenerator` 在生成/校验/刷新时联动缓存：

```java
public interface TokenCache {
    void setToken(UserClaim userClaim, AuthenticateToken authenticateToken);
    String getToken(String userId);
    void refreshToken(String userId, String token);
}
```

### 3. 认证事件订阅

`DefaultTokenGenerator` 在生成/刷新 Token 时发布 Spring 事件，接入方用 `@EventListener` 订阅：

| 事件类（`com.ddf.boot.common.core.event`） | 触发时机        | 关键字段                          |
|---------------------------------------|-------------|-------------------------------|
| `LoginSuccessEvent`                   | 生成 token 成功 | `getUserClaim()`              |
| `TokenRefreshEvent`                   | token 刷新    | `getUserId()`                 |
| `LoginFailureEvent`                   | 认证失败        | `getToken()`、`getErrorCode()` |

```java
@EventListener
public void onLoginSuccess(LoginSuccessEvent event) {
    UserClaim claim = event.getUserClaim();
    // 记录登录日志、下发通知等
}
```

## 注意事项

1. **密钥管理**：生产环境必须通过配置中心注入 `rsa-private-key` / `aes-secret` / `sign-secret`，禁止硬编码。
2. **雪花唯一性**：多实例部署时必须保证 `snowflake-worker-id` 与 `snowflake-data-center-id` 组合唯一，否则可能产生重复 ID。
3. **线程池**：经 `ThreadBuilderHelper` 构建的线程池会自动注册优雅关闭，无需手动调用 `shutdown`。
4. **静态上下文**：`SpringContextHolder` / `IdsUtil` 依赖 Spring 容器，仅能在应用上下文初始化后使用。
