# ddf-common-authentication

> 认证与授权基础模块：提供 Token 生成/校验/刷新、用户上下文、请求签名校验、
> 认证过滤器及扩展点。它是 `ddf-common-starter-web` 的组成部分，
> **业务工程一般通过 starter 间接引入**。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-authentication` 解决的是 **"谁正在访问我的接口"** 这一横切问题。
从移动端 App 到内部管理后台，所有需要身份识别的 Web 场景都适用。

| 场景       | 典型问题                    | 模块提供的能力                                                                |
|----------|-------------------------|------------------------------------------------------------------------|
| Token 认证 | 移动端登录后需要状态保持，防止伪造       | `TokenUtil.createToken` / `TokenUtil.checkToken`（AES 加密 + Redis 缓存双校验） |
| 用户上下文    | Controller 里频繁需要当前用户 ID | `UserContextUtil.getUserId()` / `getUserClaim()` ThreadLocal 上下文       |
| 请求签名校验   | 开放网关防止请求被篡改 / 重放        | `AuthenticateTokenFilter` 自动验签（HMAC-SHA256 + nonce 时间窗）                |
| 单点登出     | 用户修改密码后需要踢掉旧 Token      | `TokenCache` 接口：删除 Redis 中的 token 即可全局失效                               |
| 接口白名单    | 登录注册接口不需要认证             | `AuthenticationProperties.ignores` / `openIgnores` Ant 风格路径匹配          |
| 自定义认证逻辑  | 业务需要在标准校验前后加逻辑          | `UserClaimService` / `TokenCustomizeCheckService` 扩展接口                 |

> ⚠️ 本模块不包含 OAuth2 / SSO / SAML 等重型协议。如需 SSO，请在业务层集成 Spring Security OAuth2。

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

`ddf-common-authentication` 本身依赖 `ddf-common-mvc` + `ddf-common-redis`
（Redis 用于 token 缓存，若不需要分布式 token 校验可降级）。

---

## 3. 最小化配置

在启动类上添加 `@EnableAuthenticate`：

```java
@SpringBootApplication
@EnableAuthenticate
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

配置项（前缀 `customizer.infra.authentication`）：

```yaml
customizer:
  infra:
    authentication:
      secret: "your-32-byte-aes-secret-key-here"   # AES 加密密钥，必须配置
      sign-secret: "your-32-byte-sign-secret-here" # HMAC 签名密钥，必须配置
      expired-minute: 60                             # Token 过期时间（分钟）
      token-header-name: "ACCESS-TOKEN"              # 客户端传递 Token 的请求头
      token-prefix: ""                               # Token 前缀（如 "Bearer "）
      sign-enabled: true                             # 是否开启签名校验
      time-force-check-diff-minute: 10               # nonce 时间窗（±10 分钟）
      ignores:                                       # 内部接口白名单（Ant 风格）
        - /api/public/**
        - /health
      open-ignores:                                  # 开放接口（不校验任何逻辑）
        - /webhook/**
```

> 生产环境 `secret` 和 `sign-secret` **必须**从配置中心 / KMS 注入，禁止硬编码。
> 模块已标注 `@RefreshScope`，支持热刷新。

---

## 4. 核心 API 使用指南

### 4.1 Token 生命周期

```java
// 1. 登录成功，生成 Token
UserClaim claim = new UserClaim();
claim.setUserId("1001");
claim.setCredit("device-abc-123");   // 设备号 / 客户端标识
AuthenticateToken token = TokenUtil.createToken(claim);
// token.getToken() → 返回给客户端的完整 token 字符串

// 2. 客户端每次请求带上 header: ACCESS-TOKEN=<token>

// 3. 服务端过滤器自动校验
//    - 解析 AES 加密内容
//    - Redis 比对（防止篡改 / 单点登出）
//    - 校验 nonce 时间窗
//    - 校验 HMAC 签名

// 4. Token 续期（每次有效请求自动刷新过期时间）
TokenUtil.refreshToken(userId, tokenString);
```

### 4.2 获取当前用户

```java
@RestController
public class OrderController {
    @PostMapping("/orders")
    public ResponseData<Void> create(@RequestBody CreateOrderRequest request) {
        String userId = UserContextUtil.getUserId();           // 当前登录用户 ID
        UserClaim claim = UserContextUtil.getUserClaim();      // 完整用户声明
        String imei = UserContextUtil.getImei();               // 设备号
        String clientIp = UserContextUtil.getClientIpFromGateway();  // 客户端 IP
        // ...
    }
}
```

`UserContextUtil` 基于 `ThreadLocal` + `MDC`，过滤器链结束后自动清理，防止内存泄漏。

### 4.3 自定义认证校验

实现 `UserClaimService`：

```java
@Component
public class UserClaimServiceImpl implements UserClaimService {

    @Override
    public ResponseData<Object> beforeTokenVerify(HttpServletRequest request, HttpServletResponse response,
            Map<String, String> clientHeaderMap, Map<String, String> customizeHeaderMap) {
        // Token 解析前的预处理：如黑名单设备拦截
        String imei = clientHeaderMap.get(RequestHeaderEnum.IMEI.getName());
        if (blocklistService.contains(imei)) {
            return ResponseData.failure(BaseErrorCallbackCode.ACCESS_FORBIDDEN);
        }
        return ResponseData.success(null);
    }

    @Override
    public UserClaim getStoreUserInfo(HttpServletRequest request, UserClaim tokenClaim) {
        // 用 token 中的 userId 加载数据库最新信息
        User user = userMapper.selectById(tokenClaim.getUserId());
        tokenClaim.setNickname(user.getNickname());
        tokenClaim.setRole(user.getRole());
        return tokenClaim;
    }

    @Override
    public void afterTokenVerifySuccess(HttpServletRequest request, UserClaim userClaim,
            Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        // 认证通过后的后置处理：如写入 Spring Security Context
    }

    @Override
    public ResponseData<Object> beforeDispatch(HttpServletRequest request, HttpServletResponse response,
            UserClaim userClaim, Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        // 请求分发前的最终检查：如权限校验
        if (!permissionService.hasPermission(userClaim, request.getRequestURI())) {
            return ResponseData.failure(BaseErrorCallbackCode.ACCESS_FORBIDDEN);
        }
        return ResponseData.success(null);
    }
}
```

### 4.4 自定义 Token 校验规则

实现 `TokenCustomizeCheckService`：

```java
@Component
public class MyTokenCheckService implements TokenCustomizeCheckService {
    @Override
    public UserClaim customizeCheck(HttpServletRequest request, AuthenticateCheckResult result) {
        UserClaim claim = result.getUserClaim();
        // 例如：校验用户状态是否被禁用
        if (userService.isDisabled(claim.getUserId())) {
            throw new UnauthorizedException(BaseErrorCallbackCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        }
        return claim;
    }
}
```

### 4.5 单点登出

```java
// 用户修改密码后，删除 Redis 中的 token
stringRedisTemplate.delete("auth:token:" + userId);
// 此后该用户的所有旧 Token 都会被 `TokenUtil.checkToken` 判定为失效
```

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义 Token 缓存实现

默认使用 Redis 缓存 Token。如需替换（如改用 Caffeine 本地缓存）：

```java
@Bean
public TokenCache tokenCache() {
    return new TokenCache() {
        private final Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();

        @Override public void setToken(UserClaim claim, AuthenticateToken token) {
            cache.put(claim.getUserId(), token.getToken());
        }
        @Override public String getToken(String userId) { return cache.getIfPresent(userId); }
        @Override public void refreshToken(String userId, String token) { cache.put(userId, token); }
    };
}
```

### 5.2 万能签名（内部联调）

```yaml
customizer:
  infra:
    authentication:
      mock-sign-enabled: true
      mock-sign: "dev-only-magic-sign"
```

当 `sign-enabled: true` 且请求头中的 sign 等于 `mock-sign` 时，跳过签名校验。
> ⚠️ 生产环境必须关闭 `mock-sign-enabled`。

### 5.3 关闭自动注册过滤器

当项目需要多个拦截器并精确控制顺序时：

```yaml
customizer:
  infra:
    authentication:
      auto-register-filter: false
```

然后在业务工程中手动注册：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired private AuthenticateTokenFilter authenticateTokenFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticateTokenFilter)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/public/**", "/health");
    }
}
```

---

## 6. 与其他模块协作

| 模块                 | 协作方式                                                                                              |
|--------------------|---------------------------------------------------------------------------------------------------|
| `ddf-common-api`   | 使用 `UserClaim`、`AuthenticateToken`、`RequestHeaderEnum`、`BaseErrorCallbackCode`                    |
| `ddf-common-core`  | `TokenUtil` / `SecureUtil` / `SignatureUtil` 完成 AES/HMAC 运算；`SpringContextHolder` 获取 `TokenCache` |
| `ddf-common-mvc`   | 全局异常处理器捕获 `UnauthorizedException` / `BusinessException` 并包装为 `ResponseData`                       |
| `ddf-common-redis` | `TokenCacheImpl` 基于 `StringRedisTemplate` 实现分布式 Token 存储与续期                                       |
| `ddf-common-limit` | 限流拦截器通常在认证过滤器之后执行，通过 `UserContextUtil.getUserId()` 做用户级限流                                         |

---

## 7. FAQ

**Q1：Token 存在哪里？Cookie 还是 Header？**  
模块不做 Cookie 管理，完全依赖 HTTP Header（默认 `ACCESS-TOKEN`）。前端（App / Web / 小程序）
自行决定存储方式（localStorage、Keychain、Cookie 等），请求时带上对应 Header 即可。

**Q2：为什么 `TokenUtil` 在 `ddf-common-core` 里，而过滤器在 `ddf-common-authentication` 里？**  
`TokenUtil` 是纯工具方法（AES 加解密 + JSON 序列化），不依赖 Spring Web，因此放在 core 供更多场景复用；
`AuthenticateTokenFilter` 依赖 `HandlerInterceptor` 和 Servlet API，所以放在 authentication 模块。

**Q3：Token 过期后如何刷新？需要重新登录吗？**

- 短期 Token：模块不内置 refresh-token 机制，过期后客户端需重新登录（或业务自行实现 refresh-token）
- 长期会话：每次有效请求会自动调用 `TokenCache.refreshToken`，延长 Redis 中的过期时间

**Q4：`UserContextUtil` 在子线程里能拿到用户吗？**  
不能。`UserContextUtil` 基于 `ThreadLocal`，子线程不会继承父线程上下文。
如需在异步线程中使用，请在提交任务前手动传递 `UserClaim`：

```java
UserClaim claim = UserContextUtil.getUserClaim();
executor.execute(() -> {
    UserContextUtil.setUserClaim(claim);   // 手动还原
    try { process(); } finally { UserContextUtil.removeUserClaim(); }
});
```

**Q5：sign 验签失败怎么排查？**

1. 确认客户端和服务端的 `sign-secret` 一致（32 字节）
2. 确认 `nonce` 时间戳在 `time-force-check-diff-minute` 范围内
3. 确认请求体没有被 Nginx / CDN 修改（如自动添加空白字符）
4. 开发环境可临时开启 `mock-sign-enabled` 绕过，但禁止用于生产

**Q6：如何彻底关闭认证？**  
方法一：移除 `@EnableAuthenticate`  
方法二：保持注解但配置 `ignores: ["/**"]`  
方法三：排除自动配置：

```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.authentication.config.AuthenticationAutoConfiguration
```

---

## 8. 参考

- 源码：`filter/AuthenticateTokenFilter.java`
- 源码：`config/AuthenticationProperties.java`、`config/AuthenticationAutoConfiguration.java`
- 源码：`interfaces/UserClaimService.java`、`interfaces/TokenCustomizeCheckService.java`
- 源码（core）：`authentication/TokenUtil.java`、`authentication/TokenCache.java`
- 源码（api）：`model/authentication/UserClaim.java`、`model/authentication/AuthenticateToken.java`
