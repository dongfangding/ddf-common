# ddf-common-authentication

> Authentication and authorization foundation module: token generation/verification/refresh,
> user context, request-signature verification, authentication filter, and extension points.
> It is part of `ddf-common-starter-web`; **application services normally pull it in transitively
> through the starter**.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-authentication` answers the cross-cutting question **"who is calling my endpoint?"**
From mobile apps to internal admin dashboards, any web scenario that needs identity recognition
is a fit.

| Category | Typical Problem | What the Module Provides |
| ----- | ----- | ----- |
| Token authentication | Mobile login needs session persistence and forgery protection | `TokenUtil.createToken` / `TokenUtil.checkToken` (AES encryption + Redis cache dual verification) |
| User context | Controllers repeatedly need the current user ID | `UserContextUtil.getUserId()` / `getUserClaim()` via ThreadLocal |
| Request tamper protection | Open gateway must prevent request modification / replay | `AuthenticateTokenFilter` auto-verifies HMAC-SHA256 + nonce time window |
| Single-point logout | After password change, old tokens must be invalidated globally | `TokenCache` interface: delete the Redis entry and all old tokens become invalid |
| Whitelist endpoints | Login / register endpoints should skip auth | `AuthenticationProperties.ignores` / `openIgnores` with Ant-style path matching |
| Custom auth logic | Business needs pre/post hooks around standard verification | `UserClaimService` / `TokenCustomizeCheckService` extension interfaces |

> ⚠️ This module does **not** include OAuth2 / SSO / SAML. For SSO, integrate Spring Security OAuth2
> at the application layer.

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

`ddf-common-authentication` itself depends on `ddf-common-mvc` + `ddf-common-redis`
(Redis is used for token caching; distributed token verification can be downgraded if not needed).

---

## 3. Minimum Configuration

Add `@EnableAuthenticate` on the main class:

```java
@SpringBootApplication
@EnableAuthenticate
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Configuration (prefix `customizer.infra.authentication`):

```yaml
customizer:
  infra:
    authentication:
      secret: "your-32-byte-aes-secret-key-here"   # AES encryption key — MUST be configured
      sign-secret: "your-32-byte-sign-secret-here" # HMAC signing key — MUST be configured
      expired-minute: 60                             # Token expiry in minutes
      token-header-name: "ACCESS-TOKEN"              # Header name the client sends the token in
      token-prefix: ""                               # Token prefix (e.g. "Bearer ")
      sign-enabled: true                             # Enable signature verification
      time-force-check-diff-minute: 10               # Nonce time window (±10 minutes)
      ignores:                                       # Internal whitelist (Ant-style)
        - /api/public/**
        - /health
      open-ignores:                                  # Open endpoints (skip all checks)
        - /webhook/**
```

> In production, `secret` and `sign-secret` **must** be injected via config center / KMS — never
> hard-code them. The module carries `@RefreshScope` and supports hot reloading.

---

## 4. Core API Guide

### 4.1 Token lifecycle

```java
// 1. On login success, create a token
UserClaim claim = new UserClaim();
claim.setUserId("1001");
claim.setCredit("device-abc-123");   // device id / client identifier
AuthenticateToken token = TokenUtil.createToken(claim);
// token.getToken() → the full token string returned to the client

// 2. Client sends the token on every request via header: ACCESS-TOKEN=<token>

// 3. Server filter auto-verifies:
//    - Parse AES-encrypted content
//    - Redis comparison (prevents tampering / single-point logout)
//    - Validate nonce time window
//    - Validate HMAC signature

// 4. Token renewal (refresh expiry on every valid request)
TokenUtil.refreshToken(userId, tokenString);
```

### 4.2 Access the current user

```java
@RestController
public class OrderController {
    @PostMapping("/orders")
    public ResponseData<Void> create(@RequestBody CreateOrderRequest request) {
        String userId = UserContextUtil.getUserId();           // current logged-in user ID
        UserClaim claim = UserContextUtil.getUserClaim();      // full user claim
        String imei = UserContextUtil.getImei();               // device id
        String clientIp = UserContextUtil.getClientIpFromGateway();  // client IP
        // ...
    }
}
```

`UserContextUtil` is backed by `ThreadLocal` + `MDC`; the filter chain automatically cleans up
afterwards to prevent memory leaks.

### 4.3 Custom authentication hooks

Implement `UserClaimService`:

```java
@Component
public class UserClaimServiceImpl implements UserClaimService {

    @Override
    public ResponseData<Object> beforeTokenVerify(HttpServletRequest request, HttpServletResponse response,
            Map<String, String> clientHeaderMap, Map<String, String> customizeHeaderMap) {
        // Pre-token processing: e.g. blocklist device interception
        String imei = clientHeaderMap.get(RequestHeaderEnum.IMEI.getName());
        if (blocklistService.contains(imei)) {
            return ResponseData.failure(BaseErrorCallbackCode.ACCESS_FORBIDDEN);
        }
        return ResponseData.success(null);
    }

    @Override
    public UserClaim getStoreUserInfo(HttpServletRequest request, UserClaim tokenClaim) {
        // Load latest user info from the database using the userId from the token
        User user = userMapper.selectById(tokenClaim.getUserId());
        tokenClaim.setNickname(user.getNickname());
        tokenClaim.setRole(user.getRole());
        return tokenClaim;
    }

    @Override
    public void afterTokenVerifySuccess(HttpServletRequest request, UserClaim userClaim,
            Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        // Post-auth processing: e.g. write into Spring Security Context
    }

    @Override
    public ResponseData<Object> beforeDispatch(HttpServletRequest request, HttpServletResponse response,
            UserClaim userClaim, Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        // Final check before dispatch: e.g. permission check
        if (!permissionService.hasPermission(userClaim, request.getRequestURI())) {
            return ResponseData.failure(BaseErrorCallbackCode.ACCESS_FORBIDDEN);
        }
        return ResponseData.success(null);
    }
}
```

### 4.4 Custom token verification rules

Implement `TokenCustomizeCheckService`:

```java
@Component
public class MyTokenCheckService implements TokenCustomizeCheckService {
    @Override
    public UserClaim customizeCheck(HttpServletRequest request, AuthenticateCheckResult result) {
        UserClaim claim = result.getUserClaim();
        // e.g. check whether the user has been disabled
        if (userService.isDisabled(claim.getUserId())) {
            throw new UnauthorizedException(BaseErrorCallbackCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        }
        return claim;
    }
}
```

### 4.5 Single-point logout

```java
// After password change, delete the token from Redis
stringRedisTemplate.delete("auth:token:" + userId);
// From now on, all old tokens for this user fail TokenUtil.checkToken
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom token cache implementation

The default uses Redis. To replace it (e.g. with Caffeine local cache):

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

### 5.2 Universal signature (internal dev only)

```yaml
customizer:
  infra:
    authentication:
      mock-sign-enabled: true
      mock-sign: "dev-only-magic-sign"
```

When `sign-enabled: true` and the request header sign equals `mock-sign`, signature verification
is skipped.
> ⚠️ `mock-sign-enabled` must be **disabled** in production.

### 5.3 Disable auto-registration of the filter

When you need multiple interceptors with precise ordering:

```yaml
customizer:
  infra:
    authentication:
      auto-register-filter: false
```

Then register manually in your project:

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

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| ----- | ----- |
| `ddf-common-api` | Uses `UserClaim`, `AuthenticateToken`, `RequestHeaderEnum`, `BaseErrorCallbackCode` |
| `ddf-common-core` | `TokenUtil` / `SecureUtil` / `SignatureUtil` perform AES/HMAC operations; `SpringContextHolder` fetches `TokenCache` |
| `ddf-common-mvc` | Global exception handler catches `UnauthorizedException` / `BusinessException` and wraps them into `ResponseData` |
| `ddf-common-redis` | `TokenCacheImpl` uses `StringRedisTemplate` for distributed token storage and renewal |
| `ddf-common-limit` | Rate-limit interceptor usually runs after the auth filter and uses `UserContextUtil.getUserId()` for per-user throttling |

---

## 7. FAQ

**Q1: Where is the token stored — Cookie or Header?**  
The module does not manage cookies; it relies entirely on HTTP headers (default `ACCESS-TOKEN`).
The frontend (App / Web / Mini-program) decides how to store the token (localStorage, Keychain,
Cookie, etc.) and sends it in the corresponding header on each request.

**Q2: Why is `TokenUtil` in `ddf-common-core` while the filter is in `ddf-common-authentication`?**  
`TokenUtil` is a pure utility (AES encryption/decryption + JSON serialization) with no Spring Web
dependency, so it lives in core for broader reuse. `AuthenticateTokenFilter` needs `HandlerInterceptor`
and the Servlet API, so it lives in the authentication module.

**Q3: How is the token refreshed after expiry? Does the user need to re-login?**  
- Short-lived tokens: the module does not have a built-in refresh-token mechanism; after expiry the
  client must re-login (or the business layer implements its own refresh-token flow)
- Long-lived sessions: every valid request automatically calls `TokenCache.refreshToken`, extending
  the Redis TTL

**Q4: Can `UserContextUtil` retrieve the user in a child thread?**  
No. `UserContextUtil` is backed by `ThreadLocal`; child threads do not inherit the parent context.
If you need the user in an async thread, pass the `UserClaim` manually before submitting the task:

```java
UserClaim claim = UserContextUtil.getUserClaim();
executor.execute(() -> {
    UserContextUtil.setUserClaim(claim);   // manually restore
    try { process(); } finally { UserContextUtil.removeUserClaim(); }
});
```

**Q5: How do I debug a signature-verification failure?**  
1. Confirm client and server share the same `sign-secret` (32 bytes)
2. Confirm the `nonce` timestamp is within `time-force-check-diff-minute`
3. Confirm the request body has not been modified by Nginx / CDN (e.g. automatic whitespace insertion)
4. In development, temporarily enable `mock-sign-enabled` to bypass, but **never** in production

**Q6: How do I completely disable authentication?**  
Option 1: Remove `@EnableAuthenticate`  
Option 2: Keep the annotation but set `ignores: ["/**"]`  
Option 3: Exclude the auto-configuration:

```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.authentication.config.AuthenticationAutoConfiguration
```

---

## 8. References

- Source: `filter/AuthenticateTokenFilter.java`
- Source: `config/AuthenticationProperties.java`, `config/AuthenticationAutoConfiguration.java`
- Source: `interfaces/UserClaimService.java`, `interfaces/TokenCustomizeCheckService.java`
- Source (core): `authentication/TokenUtil.java`, `authentication/TokenCache.java`
- Source (api): `model/authentication/UserClaim.java`, `model/authentication/AuthenticateToken.java`
