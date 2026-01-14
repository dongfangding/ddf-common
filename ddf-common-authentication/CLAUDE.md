# CLAUDE.md

## 模块简介

提供 JWT Token 认证和授权功能，支持 Token 生成、校验、刷新和上下文获取。

## 核心类

| 类路径                                                                  | 功能          |
|----------------------------------------------------------------------|-------------|
| `com.ddf.boot.common.authentication.filter.AuthenticateTokenFilter`  | Token 认证过滤器 |
| `com.ddf.boot.common.authentication.util.UserContextUtil`            | 用户上下文工具     |
| `com.ddf.boot.common.core.authentication.TokenUtil`                  | Token 工具类   |
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
