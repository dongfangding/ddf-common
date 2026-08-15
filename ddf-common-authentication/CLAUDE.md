# CLAUDE.md

## 模块简介

提供 JWT Token 认证和授权功能，支持 Token 生成、校验、刷新和上下文获取。

## 核心类

| 类路径                                                                  | 功能          |
|----------------------------------------------------------------------|-------------|
| `com.ddf.boot.common.authentication.filter.AuthenticateTokenFilter`  | Token 认证过滤器 |
| `com.ddf.boot.common.authentication.util.UserContextUtil`            | 用户上下文工具     |
| `com.ddf.boot.common.core.authentication.TokenGenerator`             | Token 生成/校验策略接口 |
| `com.ddf.boot.common.core.authentication.TokenUtil`                  | Token 工具类（已弃用） |
| `com.ddf.boot.common.authentication.config.AuthenticationProperties` | 配置属性        |

## 使用说明

### 1. 启用认证

```java
@SpringBootApplication
@EnableAuthenticate  // 启用认证模块
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 配置

```yaml
customizer:
  infra:
    ext:
      authentication:
        secret: "your-256-bit-secret-key"      # Token 加密密钥（必须配置）
        expired-minute: 60                      # Token 过期时间（分钟）
        token-header-name: "ACCESS-TOKEN"       # Token 请求头名称
        token-prefix: ""                        # Token 前缀
        sign-secret: "your-sign-secret"         # 签名密钥（必须配置）
        sign-enabled: false                     # 是否开启签名验证
        ignores:                                # 忽略认证的路径
          - /api/public/**
```

### 3. 获取当前用户

```java
// 获取用户 ID
Long userId = UserContextUtil.getUserId();

// 获取用户 Claim
UserClaim userClaim = UserContextUtil.getUserClaim();

// 获取登录信息
String credit = UserContextUtil.getCredit();
String token = UserContextUtil.getRequestContext().getToken();
```

### 4. 手动生成 Token

```java
@Autowired
private TokenUtil tokenUtil;

public String createToken(Long userId, String credit) {
    UserClaim claim = new UserClaim();
    claim.setUserId(userId);
    claim.setCredit(credit);
    claim.setVersion(System.currentTimeMillis());

    return tokenUtil.createToken(claim).getToken();
}
```

### 5. 实现 Token 缓存

```java
@Component
public class MyTokenCache implements TokenCache {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void setToken(UserClaim userClaim, AuthenticateToken token) {
        String key = "auth:token:" + token.getToken();
        redisTemplate.opsForValue().set(key, token.getToken(),
            Duration.ofMinutes(60));
    }

    @Override
    public String getTokenKey(AuthenticateToken token) {
        return "auth:token:" + token.getToken();
    }
    // ... 其他方法
}
```

### 6. 自定义 Token 生成策略

模块默认注册 `DefaultTokenGenerator`（AES 加密 + 可选 TokenCache），接入方通过注入 `TokenGenerator` 类型的自定义 Bean 替换（`@ConditionalOnMissingBean`）：

```java
@Component
public class MyTokenGenerator implements TokenGenerator {

    @Override
    public AuthenticateToken createToken(UserClaim userClaim) {
        // 生成 token
    }

    @Override
    public UserClaim getUserClaim(String token) {
        // 解析 token
    }

    @Override
    public AuthenticateCheckResult checkToken(String token) {
        // 校验 token
    }

    @Override
    public void refreshToken(String userId, String token) {
        // 刷新 token
    }
}
```

> `TokenUtil` 已 `@Deprecated`，内部委托给 `TokenGenerator`，新代码请注入 `TokenGenerator` 使用。

### 7. 认证生命周期事件

模块在登录/生成 token、刷新、失败时发布 Spring 事件，接入方用 `@EventListener` 订阅：

| 事件类（`com.ddf.boot.common.core.event` 包） | 触发时机        | 关键字段              |
|-------------------------------------|-------------|-------------------|
| `LoginSuccessEvent`                 | 登录成功/生成 token | `userClaim`       |
| `TokenRefreshEvent`                 | token 刷新     | `userId`          |
| `LoginFailureEvent`                 | 认证失败        | `token`、`errorCode` |

```java
@EventListener
public void onLoginSuccess(LoginSuccessEvent event) {
    UserClaim userClaim = event.getUserClaim();
    // 记录登录日志等
}
```

## 认证流程

1. 客户端请求携带 `ACCESS-TOKEN` 请求头
2. `AuthenticateTokenFilter` 解析 Token
3. 校验 Token 有效性（加密校验 + Redis 校验）
4. 解析用户信息存入 `ThreadLocal`
5. 业务方法通过 `UserContextUtil` 获取用户
6. 过滤器链完成后清理 `ThreadLocal`

## 注意事项

1. **签名密钥**：生产环境必须配置复杂的 `sign-secret`，禁止使用默认值
2. **Token 加密**：使用 AES 加密 Token 内容，必须配置 `secret`
3. **并发安全**：`UserContextUtil` 使用 `ThreadLocal`，确保请求处理完成后清理
4. **跨线程**：子线程无法继承父线程的 `ThreadLocal`，如需传递请手动处理
