# ddf-common-authentication 接入指南

> Token 认证与上下文传递：拦截器解析 Token、校验签名、构建用户上下文，并提供可替换的生成/校验/缓存策略。

## 核心能力

| 能力 | 说明 | 关键类 / 入口 |
|------|------|--------------|
| Token 认证拦截 | 解析请求头 Token、验签、构建 `RequestContext` | `filter.AuthenticateTokenFilter` |
| 用户上下文 | `ThreadLocal` 保存用户与请求信息 | `com.ddf.boot.common.api.util.UserContextUtil` |
| 配置属性 | Token/签名/忽略路径等配置 | `config.AuthenticationProperties` |
| Token 生成策略 | 生成/校验/刷新（可替换） | `core.authentication.TokenGenerator` |
| 用户信息加载 | 从 DB 加载最新用户信息（必须实现） | `interfaces.UserClaimService` |
| 自定义校验 | 通用校验基础上的业务校验（可替换） | `interfaces.TokenCustomizeCheckService` |
| Token 缓存 | Redis 缓存 Token（可替换） | `core.authentication.TokenCache` |

## 接入方式

### 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-authentication</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 传递依赖 `ddf-common-mvc`、`ddf-common-redis`、`ddf-common-api`。

### 启用认证

认证拦截器**不会默认注册**，需在启动类（或任意配置类）加 `@EnableAuthenticate`：

```java
@SpringBootApplication
@EnableAuthenticate
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 关键配置

配置前缀 `customizer.infra.authentication`（对应 `AuthenticationProperties`）：

```yaml
customizer:
  infra:
    authentication:
      secret: "your-aes-secret"          # Token 加密密钥（必须配置）
      expired-minute: 60                 # Token 过期时间（分钟）
      token-header-name: "ACCESS-TOKEN"  # Token 请求头名称
      token-prefix: ""                   # Token 前缀
      credit-header-name: "imei"         # 校验 credit 身份的请求头
      sign-secret: "your-sign-secret"    # 加签密钥（32 位）
      sign-enabled: false                # 是否开启加签验证
      ignores:                           # 忽略认证的路径
        - /api/public/**
      open-ignores:                      # 开放平台路径，完全跳过校验
        - /callback/**
```

### 获取当前用户

```java
String userId = UserContextUtil.getUserId();          // 用户 ID
Long uid = UserContextUtil.getLongUserId();           // Long 型用户 ID
UserClaim claim = UserContextUtil.getUserClaim();     // 用户声明
String token = UserContextUtil.getRequestContext().getToken();
```

## 扩展点

### 1. UserClaimService（必须实现）

认证流程依赖该接口加载用户最新信息，**无默认实现，接入方必须提供**。四个方法中仅 `getStoreUserInfo` 为抽象方法：

```java
@Component
public class MyUserClaimService implements UserClaimService {

    @Override
    public UserClaim getStoreUserInfo(HttpServletRequest request, UserClaim userClaim) {
        // 根据 token 中的用户信息，从 DB 加载最新数据并返回
        return userService.loadUser(userClaim.getUserId());
    }

    @Override
    public void afterTokenVerifySuccess(HttpServletRequest request, UserClaim userClaim,
            Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        // 可选：把用户放入自行选择的安全框架上下文
    }
}
```

### 2. TokenGenerator 策略（可选替换）

默认使用 core 的 `DefaultTokenGenerator`（AES 加密 + TokenCache 联动），注入自定义 `TokenGenerator` Bean 即可整体替换：

```java
@Component
public class JwtTokenGenerator implements TokenGenerator {
    @Override
    public AuthenticateToken createToken(UserClaim userClaim) { /* JWT 签发 */ }
    @Override
    public UserClaim getUserClaim(String token) { /* JWT 解析 */ }
    @Override
    public AuthenticateCheckResult checkToken(String token) { /* JWT 校验 */ }
    @Override
    public void refreshToken(String userId, String token) { /* 刷新 */ }
}
```

### 3. TokenCustomizeCheckService（自定义校验）

默认实现 `DefaultTokenCheckServiceImpl`（校验 username/credit 非空、credit 与请求头一致后加载最新用户）。注入自定义实现可覆盖：

```java
@Component
public class MyTokenCheck implements TokenCustomizeCheckService {
    @Override
    public UserClaim customizeCheck(HttpServletRequest request, AuthenticateCheckResult result) {
        // 在通用校验通过后追加业务规则（如黑名单、多端互踢）
        return result.getUserClaim();
    }
}
```

### 4. TokenCache（缓存替换）

默认 `TokenCacheImpl` 使用 Redis，Key 为 `{applicationName}:authentication:token:{userId}`。实现 `TokenCache` 即可替换存储：

```java
@Component
public class MyTokenCache implements TokenCache {
    @Override
    public void setToken(UserClaim userClaim, AuthenticateToken authenticateToken) { ... }
    @Override
    public String getToken(String userId) { ... }
    @Override
    public void refreshToken(String userId, String token) { ... }
}
```

### 5. 认证事件订阅

| 事件类（`com.ddf.boot.common.core.event`） | 触发时机 | 关键字段 |
|------|------|------|
| `LoginSuccessEvent` | 生成 token 成功 | `getUserClaim()` |
| `TokenRefreshEvent` | token 刷新 | `getUserId()` |
| `LoginFailureEvent` | 认证失败 | `getToken()`、`getErrorCode()` |

```java
@EventListener
public void onLoginFailure(LoginFailureEvent event) {
    log.warn("认证失败 token={}, code={}", event.getToken(), event.getErrorCode());
}
```

## 认证流程

1. 客户端携带 `ACCESS-TOKEN` 请求头发起请求；
2. `AuthenticateTokenFilter.preHandle` 解析请求头、校验签名与重放时间戳；
3. `TokenGenerator.checkToken` 校验 Token 有效性（含缓存比对）；
4. `TokenCustomizeCheckService` 执行业务校验并加载最新用户信息；
5. 构建 `RequestContext` 存入 `ThreadLocal`，业务代码通过 `UserContextUtil` 获取；
6. `afterCompletion` 清理 `ThreadLocal` 与 MDC。

## 注意事项

1. **必须实现 `UserClaimService`**：未提供该 Bean 会在认证时抛出 `NoSuchBeanDefinitionException`。
2. **签名密钥**：生产环境必须配置复杂的 `sign-secret`，禁止使用默认值。
3. **并发安全**：`UserContextUtil` 基于 `ThreadLocal`，子线程不继承；跨线程使用需手动传递用户信息。
4. **忽略路径**：`ignores` 命中后以 `UserClaim.getDefaultUser()` 放行，注意与 `open-ignores`（完全跳过校验）区分。
