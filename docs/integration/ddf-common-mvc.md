# ddf-common-mvc 接入指南

> Web 横切能力集：全局异常处理、请求体缓存、请求验签、权限菜单扫描、日志切面与常用 MVC 装配。

## 核心能力

| 能力     | 说明                                      | 关键类 / 入口                                   |
|--------|-----------------------------------------|--------------------------------------------|
| 全局异常处理 | 捕获未处理异常，统一返回 `ResponseData`，支持 i18n 与扩展 | `exception200.AbstractExceptionHandler`    |
| 异常扩展接口 | 通知 / 接管 / 映射自定义异常                       | `exception200.ExceptionHandlerMapping`     |
| 请求体缓存  | 包装请求体支持重复读取（验签等场景）                      | `filter.CachingRequestBodyFilter`          |
| 请求验签   | 方法级 `@RequestSign` 验签 + 防重放             | `requestsign.RequestSignAccessFilterChain` |
| 权限菜单扫描 | 从注解扫描菜单/按钮权限树                           | `permissionscan.PermissionMenuScanner`     |
| 日志切面   | 控制层方法日志、慢方法回调                           | `logaccess.EnableLogAspect`                |
| 常用装配   | 事务/AOP/异步/定时/缓存、默认线程池                   | `config.CoreWebConfig`                     |

## 接入方式

### 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mvc</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 传递依赖 `ddf-common-core` 与 `spring-boot-starter-web`。`MvcAutoConfiguration` 自动装配，无需开关。

### 关键配置

mvc 模块本身无独立 `@ConfigurationProperties`，复用 core 的 `customizer.infra.global-properties`：

```yaml
customizer:
  infra:
    global-properties:
      sign-secret: "..."                            # 验签密钥（RequestSignAccessFilterChain 使用）
      exception-code-to-response-status: false      # 异常码是否同步为 HTTP 状态码
      ignore-log-exception-class-name:              # 不打印 error 日志的异常全类名
        - com.ddf.boot.common.api.exception.BusinessException
```

### 使用示例

```java
// 1. 抛异常即自动转为统一响应（需自行注册 advice 覆盖你的包，见扩展点）
throw new BusinessException(ErrorCodeEnum.XXX);

// 2. 请求验签：方法上加 @RequestSign，入参实现 BaseSign 或走 query 参数
@PostMapping("/submit")
@RequestSign(nonce = true, nonceIntervalSeconds = 60)
public ResponseData<Void> submit(@RequestBody OrderSubmitRequest req) { ... }

// 3. 日志切面：配置类加 @EnableLogAspect
@Configuration
@EnableLogAspect(slowTime = 1000)
public class LogConfig { }

// 方法上用 @Log 控制是否打印入参/结果
@Log(desc = "下单", printResult = true)
public void order() { ... }
```

## 扩展点

### 1. 全局异常处理

模块默认的 `CommonExceptionAdvice` 仅覆盖 `com.ddf.boot.common` 包，接入方需在自己的包里注册一个 `@RestControllerAdvice` 继承 `AbstractExceptionHandler`：

```java
@RestControllerAdvice(basePackages = "com.your.app")
public class MyExceptionAdvice extends AbstractExceptionHandler {
    // 继承即可复用全部异常处理逻辑，无需重写
}
```

如需在解析异常时做额外处理，可实现 `ExceptionHandlerMapping`（三个方法均为 default，按需覆盖）：

```java
@Component
public class MyExceptionMapping implements ExceptionHandlerMapping {

    @Override
    public void notifyException(HttpServletRequest request, Exception exception) {
        // 仅通知，不改变返回值：可用于告警、埋点
    }

    @Override
    public ResponseData<?> takeOverException(Exception exception) {
        // 完全接管异常返回值；若不是自己处理的类型返回 null，则继续走默认逻辑
        return null;
    }

    @Override
    public BaseCallbackCode resolveOtherException(Exception exception) {
        // 为默认逻辑无法识别的异常提供错误码映射
        return null;
    }
}
```

### 2. 横切链 `AccessFilterChain`

在日志切面拦截点注入自定义校验逻辑（多实现按 `getOrder()` 升序执行）：

```java
@Component
public class MyAccessFilter implements AccessFilterChain {
    @Override
    public Integer getOrder() { return 1; }

    @Override
    public boolean filter(ProceedingJoinPoint joinPoint, Class<?> pointClass, MethodSignature pointMethod) {
        // 返回 false 中断链路；返回 true 继续
        return true;
    }
}
```

### 3. 权限菜单扫描 `PermissionValueSelector`

`PermissionMenuScanner.scanPreAuthorizeMethods()` 扫描 `@PermissionMenu` / `@PermissionFunction` 注解生成菜单树。若要自定义「权限标识」的取值方式，提供 `PermissionValueSelector`：

```java
@Component
public class MyPermissionValueSelector implements PermissionValueSelector {
    @Override
    public String getPermission(Method method) {
        // 例如从 @PreAuthorize 解析权限表达式
        return "...";
    }
}
```

## 注意事项

1. **跨域**：`CoreWebConfig` 的 `addCorsMappings` 已废弃，建议在网关层处理跨域。
2. **异常屏蔽**：生产环境 `isMaskErrorDetails()=true` 的异常不会回吐堆栈，仅返回状态码与业务消息。
3. **响应体包装**：早期版本的 `AbstractCommonResponseBodyAdvice` / `@WrapperIgnore` / `response-body-advice` 自动包装能力已删除，请勿再引用。
4. **消息转换器**：`CoreWebConfig` 将 Jackson 转换器置于首位，避免 `ResponseData` 被强转为 `String` 报错。
