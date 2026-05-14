# ddf-common-mvc

> Spring MVC 核心支撑模块：全局异常统一处理、响应体自动包装、请求体缓存、
> 访问日志与慢接口检测、权限菜单扫描、接口签名校验、自定义参数解析器等。
> 它是 `ddf-common-starter-web` 的核心组成部分，**业务工程一般通过 starter 间接引入**。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-mvc` 定位是 **Web 层的"胶水与治理"**：所有控制器共用的横切能力都放在这里，
业务代码只需专注写接口逻辑。

| 场景         | 典型问题                             | 模块提供的能力                                                          |
|------------|----------------------------------|------------------------------------------------------------------|
| 全局异常处理     | 每个 Controller 手写 try-catch，格式不统一 | `AbstractExceptionHandler` 自动捕获并映射为 `ResponseData`               |
| 响应体统一包装    | 有的接口返回原始对象，有的手动包 `ResponseData`  | `AbstractCommonResponseBodyAdvice` 自动包装                          |
| 请求体多次读取    | 签名验签需要先读 body，后续框架再读一次会报错        | `CachingRequestBodyFilter` 缓存请求体到 `ContentCachingRequestWrapper` |
| 访问日志 + 慢接口 | 需要记录每个接口的入参、出参、耗时，超时告警           | `@EnableLogAspect` 开启 AOP 日志与慢接口回调                               |
| 权限菜单扫描     | 需要自动收集所有 controller 的权限注解生成菜单    | `PermissionMenuScanner` 扫描 `@PermissionMenu`                     |
| 接口签名防篡改    | 开放网关对接需要验签                       | `RequestSignAccessFilterChain` 基于 `BaseSign` 自动验签                |
| 自定义参数解析    | 同一个参数需要支持多种 content-type 解析      | `MultiArgumentResolver` / `QueryParamArgumentResolver`           |
| 国际化异常消息    | 异常消息需要按客户端语言返回                   | `AbstractExceptionHandler` 根据 `app_language` header 解析 Locale    |

> ⚠️ 数据库连接池、Druid 等基础设施已迁出到 `ddf-common-data-mysql-starter`。

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

如需数据库能力，再叠加：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

`ddf-common-mvc` 本身依赖 `ddf-common-core` + `spring-boot-starter-web`。

---

## 3. 最小化配置

模块无强制配置。可选配置项：

```yaml
customizer:
  infra:
    # 统一响应包装排除列表
    response-body-advice:
      ignoreReturnType:
        - com.example.SomeSpecialType   # 该类型的返回值不会被包装成 ResponseData
```

---

## 4. 核心 API 使用指南

### 4.1 全局异常处理

`MvcAutoConfiguration` 自动注入 `CommonExceptionAdvice`（继承 `AbstractExceptionHandler`）。
所有未捕获异常会被自动映射为 `ResponseData`：

```java
// 业务层随便抛
throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
throw new BadRequestException("参数错误");
throw new ServerErrorException("系统错误");

// 前端收到的永远是统一格式
// { "code": "ORDER_NOT_FOUND", "message": "订单不存在", ... }
```

异常处理行为：

| 异常类型                    | HTTP status | 是否模糊化 | 说明                                 |
|-------------------------|-------------|-------|------------------------------------|
| `BusinessException`     | 200         | 否     | 直接返回 `description`                 |
| `BadRequestException`   | 200         | 是     | 返回 `bizMessage`（默认"错误请求"）          |
| `ServerErrorException`  | 200         | 是     | 返回 `bizMessage`（默认"请求失败，请联系客服人员~"） |
| `BindException`         | 200         | 否     | 拼接所有字段校验错误                         |
| `DuplicateKeyException` | 200         | 否     | 映射为 `DUPLICATE_KEY`                |

> 开启 `customizer.infra.global-properties.exception-code-to-response-status: true` 时，
> 部分异常码会被同步写入 HTTP response.status。

### 4.2 响应体自动包装

`AbstractCommonResponseBodyAdvice`（`ResponseBodyAdvice`）会自动把 Controller 的返回值包成 `ResponseData`：

```java
@RestController
public class UserController {
    @GetMapping("/user/{id}")
    public UserVO getUser(@PathVariable Long id) {
        return userService.get(id);   // 实际输出: ResponseData.success(userVO)
    }
}
```

跳过包装的方法：

```java
@WrapperIgnore
@GetMapping("/health")
public String health() {
    return "ok";   // 直接输出 "ok"
}
```

也可通过配置排除特定返回类型：

```yaml
customizer:
  infra:
    response-body-advice:
      ignoreReturnType:
        - org.springframework.core.io.Resource
```

### 4.3 请求体缓存过滤器

`CachingRequestBodyFilter`（`@Order(Ordered.HIGHEST_PRECEDENCE)`）在过滤器链最前端把 `HttpServletRequest`
包装为 `ContentCachingRequestWrapper`，后续任意代码都能安全重复读取 body：

```java
// 在 filter / interceptor / advice 中
String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
// 后续 Spring 的 @RequestBody 依然能正常解析
```

### 4.4 访问日志与慢接口检测

在任意 `@Configuration` 上添加 `@EnableLogAspect`：

```java
@Configuration
@EnableLogAspect(slowTime = 2000)   // 超过 2s 视为慢接口
public class LogConfig {
}
```

功能：

- 自动打印 Controller 方法的入参、出参、执行耗时
- 异常时打印入参和异常信息
- 超过 `slowTime` 触发 `SlowEventAction` 回调，可用于对接告警系统

```java
@Component
public class SlowEventAlarm implements SlowEventAction {
    @Override
    public void onSlowEvent(String className, String methodName, long elapsedMs, Object param, Object result) {
        alarmService.send("慢接口: " + className + "#" + methodName + " 耗时 " + elapsedMs + "ms");
    }
}
```

忽略指定类或方法：

```java
@EnableLogAspect(slowTime = 2000, ignore = {"com.example.BatchController"})
```

### 4.5 权限菜单扫描

在 Controller 方法上标注 `@PermissionMenu`：

```java
@RestController
@PermissionMenu(name = "用户管理", type = PermissionMenuType.MENU)
public class UserController {

    @GetMapping("/users")
    @PermissionMenu(name = "查询用户", type = PermissionMenuType.FUNCTION)
    public ResponseData<List<UserVO>> list() { ... }
}
```

启动时 `PermissionMenuScanner` 会自动扫描并收集所有权限定义，业务可订阅扫描结果生成 RBAC 菜单树。

### 4.6 接口签名校验

请求 DTO 实现 `BaseSign`（来自 `ddf-common-api`），并在 filter 链中注册 `RequestSignAccessFilterChain`：

```java
@Data
public class GatewayRequest implements BaseSign {
    private Long userId;
    private String sign;          // BaseSign 要求
    private Long nonceTimestamp;  // BaseSign 要求
}
```

`RequestSignAccessFilterChain` 会：

1. 提取 `sign` 和 `nonceTimestamp`
2. 重新计算 HMAC-SHA256 签名
3. 比对失败时抛 `BusinessException(SIGN_ERROR)`
4. 检查时间戳是否过期（默认 5 分钟）

### 4.7 自定义参数解析器

`MultiArgumentResolver` 支持同一个参数按多种 content-type 解析：

```java
@PostMapping("/upload")
public ResponseData<Void> upload(@MultiArgument FileUploadRequest request) {
    // 自动根据 content-type 选择 JSON / form-data / multipart 解析方式
}
```

`QueryParamArgumentResolver` 支持把 query string 中的复杂对象按属性名自动绑定。

### 4.8 国际化异常消息

`AbstractExceptionHandler` 会根据请求头 `app_language` 解析 Locale，然后从 `MessageSource`
查找对应语言的消息。如果找不到，回退到英文默认消息。

---

## 5. 进阶用法 / 扩展点

### 5.1 接管异常处理

实现 `ExceptionHandlerMapping` 接口：

```java
@Component
public class MyExceptionHandlerMapping implements ExceptionHandlerMapping {
    @Override
    public void notifyException(HttpServletRequest request, Exception ex) {
        // 仅通知，不接管返回值
        metrics.counter("exception", "type", ex.getClass().getSimpleName()).increment();
    }

    @Override
    public ResponseData<?> takeOverException(Exception ex) {
        // 返回非 null 则直接用这个返回值，跳过默认处理
        if (ex instanceof CustomDomainException) {
            return ResponseData.failure(CustomErrorCode.DOMAIN_ERROR);
        }
        return null;   // 继续走默认逻辑
    }

    @Override
    public BaseCallbackCode resolveOtherException(Exception ex) {
        // 把非 BaseException 映射到标准错误码
        if (ex instanceof CircuitBreakerOpenException) {
            return BaseErrorCallbackCode.REQUEST_TOO_MANY;
        }
        return null;
    }
}
```

### 5.2 自定义响应包装逻辑

继承 `AbstractCommonResponseBodyAdvice`，添加自己的包过滤规则：

```java
@RestControllerAdvice(basePackages = "com.example.controller")
public class CustomResponseBodyAdvice extends AbstractCommonResponseBodyAdvice {
    // 自动继承 beforeBodyWrite 的 ResponseData.success(body) 逻辑
}
```

### 5.3 监听全局异常事件

```java
@EventListener
public void onGlobalException(GlobalExceptionEvent event) {
    GlobalExceptionEventPayload p = event.getPayload();
    // p 包含: url, parameterMap, body, host, applicationName, profile, errorCode, errorMessage, stackTrace
    alarmService.send(p.getErrorCode(), p.getErrorMessage());
}
```

---

## 6. 与其他模块协作

| 模块                          | 协作方式                                                                                    |
|-----------------------------|-----------------------------------------------------------------------------------------|
| `ddf-common-api`            | 异常体系 (`BaseException` / `ResponseData`)、签名接口 (`BaseSign`)、请求头枚举 (`RequestHeaderEnum`)   |
| `ddf-common-core`           | 复用 `GlobalProperties` (异常码映射 status、忽略日志异常类名)、`EnvironmentHelper`、`SpringContextHolder` |
| `ddf-common-authentication` | 鉴权异常 (`UnauthorizedException` / `AccessDeniedException`) 被全局异常处理器统一包装                   |
| `ddf-common-redis`          | 签名验签使用的 `sign-secret` 通过 `GlobalProperties` 读取                                          |
| `ddf-common-limit`          | 限流拦截器抛出的异常被全局异常处理器捕获并包装                                                                 |
| `ddf-common-starter-web`    | 本模块是 starter 的 Web 层核心                                                                  |

---

## 7. FAQ

**Q1：为什么返回值已经是 `ResponseData` 了，还会被再次包装？**  
`AbstractCommonResponseBodyAdvice` 的 `beforeBodyWrite` 里会判断 body 类型；
如果已经是 `ResponseData` 则直接透传，不会套娃。如果出现了套娃，请检查是否自定义了
`ResponseBodyAdvice` 并且优先级高于默认实现。

**Q2：`@WrapperIgnore` 和 `ignoreReturnType` 有什么区别？**

- `@WrapperIgnore`：注解在方法上，粒度细，即开即用
- `ignoreReturnType`：配置在 YAML 中，按返回类型的全类名排除，适合排除第三方框架的返回值

**Q3：慢接口回调 `SlowEventAction` 是同步还是异步？**  
同步执行（在 AOP 的 `afterReturning` 中）。如果回调逻辑较重（如发告警邮件），
请在实现内部自己丢到线程池异步处理。

**Q4：`CachingRequestBodyFilter` 会不会导致内存溢出？**  
它基于 Spring 的 `ContentCachingRequestWrapper`，默认缓存到 `byte[]` 中。
超大文件上传场景（如几百 MB）建议走 `/multipart` 专用接口并排除该 filter，
或在 Nginx 层直接拦截大文件请求。

**Q5：异常堆栈在生产环境被隐藏了，如何排查？**  
日志文件（`error` 级别）中仍然保留了完整堆栈和请求参数（受 `ignoreLogExceptionClassName` 控制）。
`ResponseData.subMessage` 只是前端展示层面的隐藏。

**Q6：如何关闭响应体自动包装？**  
在业务工程的 `@Configuration` 中声明一个更高优先级的 `ResponseBodyAdvice` 返回原始 body，
或排除 `MvcAutoConfiguration`：

```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.mvc.config.MvcAutoConfiguration
```

---

## 8. 参考

- 源码：`exception200/AbstractExceptionHandler.java`、`exception200/CommonExceptionAdvice.java`
- 源码：`controllerwrapper/AbstractCommonResponseBodyAdvice.java`
- 源码：`filter/CachingRequestBodyFilter.java`
- 源码：`logaccess/AccessLogAspect.java`、`logaccess/EnableLogAspect.java`
- 源码：`permissionscan/PermissionMenuScanner.java`
- 源码：`requestsign/RequestSignAccessFilterChain.java`
- 源码：`resolver/MultiArgumentResolver.java`
