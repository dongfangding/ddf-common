# ddf-common-mvc

> Spring MVC core support module: unified global exception handling, automatic response-body wrapping,
> request-body caching, access logging and slow-endpoint detection, permission-menu scanning,
> request-signature verification, custom argument resolvers, and more.
> It is the web-layer heart of `ddf-common-starter-web`; **application services normally pull it in
> transitively through the starter**.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-mvc` is the **glue and governance layer** for the web tier. All cross-cutting concerns
shared by controllers live here, so business code can focus purely on endpoint logic.

| Category                        | Typical Problem                                                                        | What the Module Provides                                              |
|---------------------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| Global exception handling       | Hand-written try-catch in every controller, inconsistent formats                       | `AbstractExceptionHandler` auto-captures and maps to `ResponseData`   |
| Unified response wrapping       | Some endpoints return raw objects, others manually wrap `ResponseData`                 | `AbstractCommonResponseBodyAdvice` auto-wraps                         |
| Multiple request-body reads     | Signature verification needs to read the body first, then the framework reads it again | `CachingRequestBodyFilter` wraps with `ContentCachingRequestWrapper`  |
| Access logging + slow endpoints | Need to log every endpoint's params, result, and elapsed time; alert on timeout        | `@EnableLogAspect` enables AOP logging and slow-event callbacks       |
| Permission-menu scanning        | Need to auto-collect all `@PermissionMenu` annotations to build an RBAC menu tree      | `PermissionMenuScanner` scans `@PermissionMenu` on controllers        |
| Request tamper protection       | Open-gateway integration needs signature verification                                  | `RequestSignAccessFilterChain` auto-verifies based on `BaseSign`      |
| Custom argument resolution      | Same parameter needs to support multiple content-type parsers                          | `MultiArgumentResolver` / `QueryParamArgumentResolver`                |
| i18n error messages             | Error messages should be returned in the client's language                             | `AbstractExceptionHandler` resolves Locale from `app_language` header |

> ⚠️ Database connection pools, Druid, and similar infrastructure have been moved to
> `ddf-common-data-mysql-starter`.

---

## 2. Maven Dependency

Application services should **not** depend on this module directly; use the starter instead:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Add database support when needed:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

`ddf-common-mvc` itself depends on `ddf-common-core` + `spring-boot-starter-web`.

---

## 3. Minimum Configuration

No mandatory configuration. Optional settings:

```yaml
customizer:
  infra:
    # Exclude specific return types from unified response wrapping
    response-body-advice:
      ignoreReturnType:
        - com.example.SomeSpecialType
```

---

## 4. Core API Guide

### 4.1 Global exception handling

`MvcAutoConfiguration` auto-injects `CommonExceptionAdvice` (extends `AbstractExceptionHandler`).
All uncaught exceptions are automatically mapped to `ResponseData`:

```java
// Business layer throws freely
throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
throw new BadRequestException("Invalid parameter");
throw new ServerErrorException("System error");

// Frontend always receives a uniform shape
// { "code": "ORDER_NOT_FOUND", "message": "Order not found", ... }
```

Exception handling behavior:

| Exception type          | HTTP status | Masked? | Notes                                                                    |
|-------------------------|-------------|---------|--------------------------------------------------------------------------|
| `BusinessException`     | 200         | No      | Returns `description` directly                                           |
| `BadRequestException`   | 200         | Yes     | Returns `bizMessage` (default: "Bad request")                            |
| `ServerErrorException`  | 200         | Yes     | Returns `bizMessage` (default: "Request failed, please contact support") |
| `BindException`         | 200         | No      | Concatenates all field validation errors                                 |
| `DuplicateKeyException` | 200         | No      | Maps to `DUPLICATE_KEY`                                                  |

> When `customizer.infra.global-properties.exception-code-to-response-status: true`, some exception
> codes are also mirrored to the HTTP response.status.

### 4.2 Automatic response-body wrapping

`AbstractCommonResponseBodyAdvice` (`ResponseBodyAdvice`) automatically wraps controller return
values into `ResponseData`:

```java
@RestController
public class UserController {
    @GetMapping("/user/{id}")
    public UserVO getUser(@PathVariable Long id) {
        return userService.get(id);   // Actual output: ResponseData.success(userVO)
    }
}
```

Skip wrapping on a method:

```java
@WrapperIgnore
@GetMapping("/health")
public String health() {
    return "ok";   // Raw output: "ok"
}
```

Or exclude by return type via configuration:

```yaml
customizer:
  infra:
    response-body-advice:
      ignoreReturnType:
        - org.springframework.core.io.Resource
```

### 4.3 Request-body caching filter

`CachingRequestBodyFilter` (`@Order(Ordered.HIGHEST_PRECEDENCE)`) wraps `HttpServletRequest` with
`ContentCachingRequestWrapper` at the very front of the filter chain, so any downstream code can
safely re-read the body:

```java
// In a filter / interceptor / advice
String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
// Spring's @RequestBody still works normally afterwards
```

### 4.4 Access logging and slow-endpoint detection

Add `@EnableLogAspect` on any `@Configuration`:

```java
@Configuration
@EnableLogAspect(slowTime = 2000)   // Treat > 2 s as slow
public class LogConfig {
}
```

Features:

- Auto-prints controller method params, result, and elapsed time
- On exception, prints params and the exception
- When elapsed time exceeds `slowTime`, triggers the `SlowEventAction` callback — hook it into
  your alerting pipeline

```java
@Component
public class SlowEventAlarm implements SlowEventAction {
    @Override
    public void onSlowEvent(String className, String methodName, long elapsedMs, Object param, Object result) {
        alarmService.send("Slow endpoint: " + className + "#" + methodName + " took " + elapsedMs + "ms");
    }
}
```

Ignore specific classes or methods:

```java
@EnableLogAspect(slowTime = 2000, ignore = {"com.example.BatchController"})
```

### 4.5 Permission-menu scanning

Annotate controller methods with `@PermissionMenu`:

```java
@RestController
@PermissionMenu(name = "User Management", type = PermissionMenuType.MENU)
public class UserController {

    @GetMapping("/users")
    @PermissionMenu(name = "Query Users", type = PermissionMenuType.FUNCTION)
    public ResponseData<List<UserVO>> list() { ... }
}
```

At startup `PermissionMenuScanner` auto-collects all permission definitions; your business code can
subscribe to the scan result to generate an RBAC menu tree.

### 4.6 Request-signature verification

The request DTO implements `BaseSign` (from `ddf-common-api`), and `RequestSignAccessFilterChain`
is registered in the filter chain:

```java
@Data
public class GatewayRequest implements BaseSign {
    private Long userId;
    private String sign;          // Required by BaseSign
    private Long nonceTimestamp;  // Required by BaseSign
}
```

`RequestSignAccessFilterChain` will:

1. Extract `sign` and `nonceTimestamp`
2. Re-compute the HMAC-SHA256 signature
3. Throw `BusinessException(SIGN_ERROR)` on mismatch
4. Check whether the timestamp has expired (default: 5 minutes)

### 4.7 Custom argument resolvers

`MultiArgumentResolver` allows the same parameter to be parsed from multiple content types:

```java
@PostMapping("/upload")
public ResponseData<Void> upload(@MultiArgument FileUploadRequest request) {
    // Automatically selects JSON / form-data / multipart parsing based on content-type
}
```

`QueryParamArgumentResolver` supports binding complex objects from query-string parameters by
property name.

### 4.8 i18n exception messages

`AbstractExceptionHandler` resolves Locale from the `app_language` request header and looks up the
corresponding message from `MessageSource`. Falls back to English if no translation is found.

---

## 5. Advanced Usage / Extension Points

### 5.1 Take over exception handling

Implement `ExceptionHandlerMapping`:

```java
@Component
public class MyExceptionHandlerMapping implements ExceptionHandlerMapping {
    @Override
    public void notifyException(HttpServletRequest request, Exception ex) {
        // Notification only; does not override the return value
        metrics.counter("exception", "type", ex.getClass().getSimpleName()).increment();
    }

    @Override
    public ResponseData<?> takeOverException(Exception ex) {
        // Return non-null to bypass default handling entirely
        if (ex instanceof CustomDomainException) {
            return ResponseData.failure(CustomErrorCode.DOMAIN_ERROR);
        }
        return null;   // Continue with default logic
    }

    @Override
    public BaseCallbackCode resolveOtherException(Exception ex) {
        // Map non-BaseException types to standard error codes
        if (ex instanceof CircuitBreakerOpenException) {
            return BaseErrorCallbackCode.REQUEST_TOO_MANY;
        }
        return null;
    }
}
```

### 5.2 Custom response-wrapping logic

Extend `AbstractCommonResponseBodyAdvice` with your own package filter:

```java
@RestControllerAdvice(basePackages = "com.example.controller")
public class CustomResponseBodyAdvice extends AbstractCommonResponseBodyAdvice {
    // Inherits beforeBodyWrite logic: ResponseData.success(body)
}
```

### 5.3 Subscribe to global exception events

```java
@EventListener
public void onGlobalException(GlobalExceptionEvent event) {
    GlobalExceptionEventPayload p = event.getPayload();
    // p contains: url, parameterMap, body, host, applicationName, profile, errorCode, errorMessage, stackTrace
    alarmService.send(p.getErrorCode(), p.getErrorMessage());
}
```

---

## 6. Interplay with Other Modules

| Module                      | How They Cooperate                                                                                                               |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| `ddf-common-api`            | Exception taxonomy (`BaseException` / `ResponseData`), signing interface (`BaseSign`), request-header enum (`RequestHeaderEnum`) |
| `ddf-common-core`           | Reuses `GlobalProperties` (status mapping, ignored log exception classes), `EnvironmentHelper`, `SpringContextHolder`            |
| `ddf-common-authentication` | Auth exceptions (`UnauthorizedException` / `AccessDeniedException`) are uniformly wrapped by the global handler                  |
| `ddf-common-redis`          | Signature verification reads `sign-secret` from `GlobalProperties`                                                               |
| `ddf-common-limit`          | Rate-limit interceptor exceptions are caught and wrapped by the global handler                                                   |
| `ddf-common-starter-web`    | This module is the web-layer core of that starter                                                                                |

---

## 7. FAQ

**Q1: My return value is already `ResponseData`, why isn't it wrapped again?**  
`AbstractCommonResponseBodyAdvice#beforeBodyWrite` checks the body type; if it is already a
`ResponseData`, it is passed through as-is. If you see double wrapping, check whether a custom
`ResponseBodyAdvice` is running with higher precedence than the default one.

**Q2: What's the difference between `@WrapperIgnore` and `ignoreReturnType`?**

- `@WrapperIgnore`: Method-level annotation, fine-grained, use on-the-fly
- `ignoreReturnType`: YAML configuration, excludes by fully-qualified return-type class name,
  useful for third-party framework return types

**Q3: Is `SlowEventAction` invoked synchronously or asynchronously?**  
Synchronously (inside the AOP `afterReturning` advice). If your callback is heavy (e.g. sending
alert emails), dispatch it to a thread pool inside your implementation.

**Q4: Can `CachingRequestBodyFilter` cause an OOM?**  
It is based on Spring's `ContentCachingRequestWrapper`, which caches into a `byte[]` in memory.
For very large file uploads (hundreds of MB), use a dedicated `/multipart` endpoint and exclude
the filter, or intercept large uploads at the Nginx layer.

**Q5: Stack traces are hidden in production — how do I debug?**  
Log files (at `error` level) still retain the full stack trace and request parameters (subject to
`ignoreLogExceptionClassName`). `ResponseData.subMessage` is hidden only at the frontend level.

**Q6: How do I disable automatic response wrapping entirely?**  
Declare a higher-precedence `ResponseBodyAdvice` in your own project that returns the raw body, or
exclude `MvcAutoConfiguration`:

```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.mvc.config.MvcAutoConfiguration
```

---

## 8. References

- Source: `exception200/AbstractExceptionHandler.java`, `exception200/CommonExceptionAdvice.java`
- Source: `controllerwrapper/AbstractCommonResponseBodyAdvice.java`
- Source: `filter/CachingRequestBodyFilter.java`
- Source: `logaccess/AccessLogAspect.java`, `logaccess/EnableLogAspect.java`
- Source: `permissionscan/PermissionMenuScanner.java`
- Source: `requestsign/RequestSignAccessFilterChain.java`
- Source: `resolver/MultiArgumentResolver.java`
