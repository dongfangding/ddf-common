# ddf-common 接入指南

> ddf-common 是面向 Spring Boot 3 的多模块通用基础组件库（Java 17），以 starter + 自动配置形式对外输出能力，由上层服务按需组合。

本文件是接入指南的总入口，聚合各模块的核心能力与扩展点。各模块的详细接入指南见 [`docs/integration/`](./docs/integration/)。

---

## 1. 快速开始

### 选择 starter

| 场景 | 选用 |
|------|------|
| 纯 Web / 不接 DB / 不接治理 | `ddf-common-starter-web` |
| 常规业务（Web + MySQL + 治理） | `ddf-common-starter-default` |
| 需自定义组合 | 按需拼装 `starter-web` + `data-mysql-starter` + `governance-starter` 等 |

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 版本号由 `ddf-common-dependency` BOM 统一管理，子模块依赖无需写版本。

### 架构分层

依赖方向只能自下而上：

```
聚合 / 依赖管理层   dependency (BOM) · starter-web · starter-default
        ↓
场景 / 扩展层       alarm  captcha  ids-service  rocketmq  ons  mqtt  websocket  ...
        ↓
基础设施层         redis  distributed-lock  data-mysql-starter  governance-starter  ...
        ↓
基础内核层         api ← core ← mvc / authentication / limit
```

---

## 2. 模块接入指南索引

| 模块 | 定位 | 核心扩展点 |
|------|------|-----------|
| [ddf-common-core](./docs/integration/ddf-common-core.md) | 轻量公共能力：加密、ID、线程池、Spring 支撑 | `TokenGenerator` 策略、`TokenCache`、认证事件 |
| [ddf-common-mvc](./docs/integration/ddf-common-mvc.md) | Web 层横切治理：异常、请求体缓存、验签、权限扫描 | `AbstractExceptionHandler` 继承、`ExceptionHandlerMapping` |
| [ddf-common-authentication](./docs/integration/ddf-common-authentication.md) | JWT 认证/授权 | `UserClaimService`（必实现）、`TokenGenerator`、`TokenCustomizeCheckService`、认证事件 |
| [ddf-common-alarm](./docs/integration/ddf-common-alarm.md) | 统一告警（钉钉/Lark） | `AlarmChannel` SPI、`AlarmFrequencyControl` |
| [ddf-common-limit](./docs/integration/ddf-common-limit.md) | 接口限流 + 防重复提交 | `RateLimitKeyGenerator`、`RateLimitAlgorithm`、`RateLimitTriggeredEvent` |
| [ddf-common-captcha](./docs/integration/ddf-common-captcha.md) | 图形/数学/滑动/点选验证码 | `CaptchaProducer` 类型分发 SPI、`CaptchaVerifyEvent` |
| [ddf-common-ids-service](./docs/integration/ddf-common-ids-service.md) | 分布式 ID（雪花 + 号段） | `IDGen` 策略注册、`IdGenRegistry` |

其它模块（redis、distributed-lock、sharding、rocketmq、websocket、s3 等）的接入说明暂见各模块根目录的 `CLAUDE.md`，后续按需补充到本指南。

---

## 3. 通用约定

### 3.1 配置属性前缀

- 规范前缀：`customizer.<scope>.<feature>`，由 `@EnableConfigurationProperties` 在对应 `*AutoConfiguration` 中显式启用。
- 常见示例：`customizer.infra.authentication`、`customizer.infra.captcha`、`customizer.infra.ids`、`customizer.infra.global-properties`、`customizer.data.mysql`、`customizer.governance`。

### 3.2 异常与响应

```java
throw new BusinessException(ErrorCodeEnum.XXX);              // 业务异常
ResponseData.success(data);                                  // 成功
ResponseData.failure(BaseErrorCallbackCode.BAD_REQUEST);     // 失败
responseData.requireSuccess();                               // 调用方强制断言
```

错误码枚举须实现 `BaseCallbackCode`。生产环境 `isMaskErrorDetails()=true` 的异常不回吐堆栈。

### 3.3 Redis Key

所有 Key 由实现 `RedisKeyConstraint` 接口的枚举生成，模式 `{applicationName}:{module}:{action}:{identifier}`，通过 `ApplicationNamedKeyGenerator.genKey(...)` 拼接。

### 3.4 自动配置

- 类名 `*AutoConfiguration`，注册在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（**不用** `spring.factories`）。
- 默认 Bean 用 `@ConditionalOnMissingBean` 暴露覆盖点；多实现用 `ObjectProvider<T>`/`List<T>`/`Map<String,T>` 聚合，供接入方注册自定义实现。

---

## 4. 通用扩展模式

接入方适配本组件库的常用手段：

```java
// 1. 替换默认实现（注册同名 Bean）
@Bean
public TokenGenerator myTokenGenerator() {
    return new MyTokenGenerator();
}

// 2. 追加自定义实现（多实现聚合，框架按序分发）
@Component
public class WeComAlarmChannel implements AlarmChannel { ... }

// 3. 订阅事件
@EventListener
public void onLogin(LoginSuccessEvent event) { ... }
```

详见各模块接入指南的「扩展点」章节。
