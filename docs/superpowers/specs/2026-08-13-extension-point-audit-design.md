# ddf-common 模块扩展点审核报告

> 日期：2026-08-13（修订版）
> 范围：根 `pom.xml` `<modules>` 声明顺序，共 32 个模块
> 目标：将每个模块作为「仅依赖、不提供源码」的第三方 jar 使用时，保证既有功能开箱可用，同时让接入方能按需适配调整。

---

## 1. 背景与目标

`ddf-common` 以 starter + 自动配置形式对外输出能力。接入方只拿到编译后的 jar（无源码），所有「可适配调整」的能力必须通过**公开扩展点**暴露，不能依赖改源码。

本报告对 32 个模块逐一审核，回答三个问题：提供什么功能、已有哪些扩展点、还缺哪些扩展点。

## 2. 扩展点分类标尺

| # | 类别        | Spring Boot 机制                                                                        | 典型适用                     |
|---|-----------|---------------------------------------------------------------------------------------|--------------------------|
| 1 | 配置扩展      | `@ConfigurationProperties`（前缀 `customizer.<scope>.<feature>`）                         | 开关、阈值、超时、地址、默认值          |
| 2 | Bean 覆盖   | `@ConditionalOnMissingBean` 声明的默认 Bean，接入方注册同名 Bean 替换                                | 整实现替换                    |
| 3 | 策略/SPI 接口 | 接口抽象 + 默认实现 + 多实现聚合（`ObjectProvider<T>`/`List<T>`，可选 `META-INF/services`）             | 算法可变、多策略并存、按类型分发         |
| 4 | 事件扩展      | `ApplicationEventPublisher` 发布 + `@EventListener`/`@TransactionalEventListener` 订阅    | 生命周期钩子、业务节点副作用（审计/通知/回调） |
| 5 | 模板方法钩子    | 抽象基类 protected 可覆写方法                                                                  | 流程固定但局部步骤可变              |
| 6 | 拦截器/后处理器  | `HandlerInterceptor`、`BeanPostProcessor`、`WebMvcConfigurer`、`ApplicationRunner` 等框架回调 | 请求拦截、Bean 初始化后处理、启动回调    |

**优先级**：P0 阻塞接入 / P1 应补 / P2 可选。

## 3. 范围决策

### 3.1 忽略的模块（不做扩展点改动）

`ddf-common-api`、`ddf-common-redis`、`ddf-common-sharding`、`ddf-common-rocketmq`、`ddf-common-xxl-executor`、`ddf-common-data-mysql-starter`、`ddf-common-websocket`、`ddf-common-netty-broker`、`ddf-common-mongo`、`ddf-common-third-party`、`ddf-common-script`、`ddf-common-es`、`ddf-common-ons`、`ddf-common-vps`、`ddf-common-mqtt`、`ddf-common-mqtt-client`、`ddf-common-canal`。

### 3.2 保留并处理的模块

| 模块                              | 改动要点                                  | 优先级 |
|---------------------------------|---------------------------------------|-----|
| `ddf-common-core`               | 雪花 workerId 分配策略、线程池指标（SecureUtil 忽略） | P2  |
| `ddf-common-mvc`                | 删除 ResponseBodyAdvice 相关逻辑            | —   |
| `ddf-common-authentication`     | 认证生命周期事件、Token 生成策略接口化                | P1  |
| `ddf-common-alarm`              | 告警渠道 SPI、频率控制                         | P1  |
| `ddf-common-limit`              | 限流算法 SPI、限流触发事件                       | P1  |
| `ddf-common-captcha`            | 验证码类型分发 SPI、校验事件                      | P1  |
| `ddf-common-ids-service`        | ID 生成策略注册                             | P1  |
| `ddf-common-distributed-lock`   | 锁生命周期事件、默认实现选择                        | P2  |
| `ddf-common-governance-starter` | 邮件模板策略                                | P2  |
| `ddf-common-zookeeper`          | 节点变更事件                                | P2  |
| `ddf-common-s3`                 | 上传/下载钩子                               | P2  |

`ddf-common-dependency`、`ddf-common-starter-web`、`ddf-common-starter-default`、`ddf-common-log4j` 无需扩展点改动（纯 BOM / 聚合 / 配置资源）。

---

## 4. 逐模块改动清单

### 4.1 `ddf-common-core`

- **忽略**：`SecureUtil` 密钥来源抽象（原 P1 建议撤销）。
- **保留（P2）**：
    - **雪花 workerId 分配策略**（第 3 类）：`IdsUtil` 的 workerId/dataCenterId 来自静态配置，多实例易冲突。抽象 `WorkerIdAssigner` 接口，默认读配置，接入方注册自定义实现（ZK/DB 分配）。
    - **线程池统一指标注册**（第 6 类）：`ThreadBuilderHelper` 创建的池缺少统一的命名/指标注册钩子。
- **说明**：`TokenUtil`（`com.ddf.boot.common.core.authentication.TokenUtil`，静态 `createToken`/`checkToken`/`refreshToken`）的 Token 生成策略接口化，见 §4.3 authentication。

### 4.2 `ddf-common-mvc`

- **删除**：`ResponseBodyAdvice` 相关逻辑 —— `controllerwrapper` 包（`CommonResponseBodyAdviceDemo`、`CommonResponseBodyAdviceProperties`、`AbstractCommonResponseBodyAdvice`）、`MvcAutoConfiguration` 中对应注册、`customizer.infra.response-body-advice` 配置前缀。
- **已具备，无需补**：
    - 国际化：`MessageSourceUtil` + `AbstractExceptionHandler`（exception200 包）。
    - 异常体系：`AbstractExceptionHandler` + `ExceptionHandlerMapping` + `CommonExceptionAdvice`，接入方继承 `AbstractExceptionHandler` 即可扩展。

### 4.3 `ddf-common-authentication`

- **认证生命周期事件（P1，第 4 类）**：当前无任何事件发布。在 `AuthenticateTokenFilter` / 认证服务中通过 `ApplicationEventPublisher` 发布 `LoginSuccessEvent`、`LoginFailureEvent`、`LogoutEvent`、`TokenRefreshEvent`，接入方用 `@EventListener` 订阅做审计、单点登录通知、黑名单联动。
- **Token 生成策略（P1，第 3 类）**：抽象 `TokenGenerator`（或 `TokenStrategy`）接口，把 `TokenUtil` 现有生成/校验/刷新逻辑作为默认实现 `DefaultTokenGenerator`（`@ConditionalOnMissingBean`），接入方可替换算法、claims 结构、密钥来源。

### 4.4 `ddf-common-alarm`

- **告警渠道 SPI（P1，第 3 类）**：当前 `DingTalkUtil`、`LarkUtil` 为静态工具类硬编码渠道。抽象 `AlarmChannel` 接口，钉钉/飞书改造为实现类，`List<AlarmChannel>` 聚合分发；接入方注册自定义渠道（企业微信/短信/邮件）。
- **告警频率控制/降级（P1，第 3/1 类）**：缺少限流、聚合、静默窗口能力，易告警风暴。提供频率控制策略（可配阈值 + 可替换策略）。

### 4.5 `ddf-common-limit`

- **限流算法 SPI（P1，第 3 类）**：当前滑动窗口/令牌桶/漏桶算法写死在 `RedisTemplateHelper`/`RateLimitAspect` 内。抽象 `RateLimitAlgorithm` 接口，现有算法改造为实现类，切面按注解选择算法，接入方新增算法（固定窗口、自定义）。
- **限流触发事件（P1，第 4 类）**：触发限流时发布事件，接入方做告警/熔断/降级。
- **已具备**：`RateLimitKeyGenerator` 已是策略接口（`Identity`/`Global`/`Ip` 三个实现）。

### 4.6 `ddf-common-captcha`

- **验证码类型分发 SPI（P1，第 3 类）**：`CaptchaHelper.generate` 用 `switch(captchaType)` 硬编码类型。抽象 `CaptchaProducer`/`CaptchaTypeHandler` 接口，按 `CaptchaType` 分发，接入方注册新验证码类型（短信/语音）。
- **校验成功/失败事件（P1，第 4 类）**：校验结果发布事件，供接入方做风控埋点。
- **已具备**：`CaptchaCacheService` SPI（`META-INF/services`）、`CaptchaHelper`/`CaptchaService`/`CacheAdapter` 的 `@ConditionalOnMissingBean`。

### 4.7 `ddf-common-ids-service`

- **ID 生成策略注册（P1，第 3 类）**：已有 `IDGen` 接口（`SnowflakeIDGenImpl`/`SegmentIDGenImpl` 均实现），但 `IdsApiImpl` 写死 cast 到这两个具体类。改为按业务码分发的策略注册（`Map<String, IDGen>` / `List<IDGen>`），接入方注册自定义实现（美团 Leaf、滴滴 Tinyid 等）。
- **保留（P2）**：WorkerId 分配策略、号段耗尽回退策略。

### 4.8 `ddf-common-distributed-lock`（P2）

- **锁生命周期事件（第 4 类）**：锁获取/释放/超时发布事件，供监控埋点。
- **默认实现选择（第 1 类）**：Redis/ZK 双 Bean 并存时缺 `@ConditionalOnProperty`/`@Primary` 选择机制。
- **已具备**：`DistributedLock` 接口 + Redis/ZK 双实现（`@ConditionalOnMissingBean`）。

### 4.9 `ddf-common-governance-starter`（P2）

- **邮件模板/渲染策略（第 3 类）**：`MailService` 的模板引擎不可替换，抽象模板渲染策略。

### 4.10 `ddf-common-zookeeper`（P2）

- **节点变更事件（第 4 类）**：`NodeEventListener` 已是接口，补基于 Spring 事件的分发封装。

### 4.11 `ddf-common-s3`（P2）

- **上传/下载钩子（第 3/4 类）**：鉴权、审计、病毒扫描等前后处理钩子。

---

## 5. 优先级汇总

### 5.1 P1 清单（应补，接入方常见适配诉求）

| 模块             | 改动                             | 类别  |
|----------------|--------------------------------|-----|
| authentication | 认证生命周期事件（登录/登出/刷新）             | 4   |
| authentication | Token 生成策略接口 + 默认实现            | 3   |
| alarm          | 告警渠道 SPI（`AlarmChannel`）       | 3   |
| alarm          | 告警频率控制/降级                      | 3/1 |
| limit          | 限流算法 SPI（`RateLimitAlgorithm`） | 3   |
| limit          | 限流触发事件                         | 4   |
| captcha        | 验证码类型分发 SPI                    | 3   |
| captcha        | 校验成功/失败事件                      | 4   |
| ids-service    | ID 生成策略注册（按业务码分发）              | 3   |

### 5.2 删除项

| 模块  | 改动                                                                                   |
|-----|--------------------------------------------------------------------------------------|
| mvc | 删除 `ResponseBodyAdvice` 相关逻辑（`controllerwrapper` 包 + 注册 + `response-body-advice` 前缀） |

### 5.3 P2 清单（可选）

core（workerId 分配、线程池指标）、distributed-lock（锁事件、默认实现选择）、governance-starter（邮件模板）、zookeeper（节点事件）、s3（上传/下载钩子）。

---

## 6. 附录：扩展点落地模式参考

补扩展点时统一遵循 Spring Boot 3 惯用法：

```java
// 模式 A：策略接口 + 默认实现 + Bean 覆盖
public interface TokenGenerator {
    AuthenticateToken createToken(UserClaim claim);
    AuthenticateCheckResult checkToken(String token);
}

@Configuration(proxyBeanMethods = false)
public class AuthenticationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(TokenGenerator.class)
    public TokenGenerator defaultTokenGenerator(AuthenticationProperties props) {
        return new DefaultTokenGenerator(props); // 现有 TokenUtil 逻辑迁入，接入方可注册同名 Bean 替换
    }
}

// 模式 B：多实现聚合（接入方注册多个，框架聚合分发）
@Bean
@ConditionalOnMissingBean
public AlarmDispatcher alarmDispatcher(ObjectProvider<AlarmChannel> channels) {
    return new AlarmDispatcher(channels.orderedStream().toList()); // 按 @Order 排序分发
}

// 模式 C：事件发布（库内部发布，接入方订阅）
public class AuthenticateTokenFilter implements HandlerInterceptor {
    private final ApplicationEventPublisher publisher;
    // ... 校验通过后
    publisher.publishEvent(new LoginSuccessEvent(userClaim));
    // 接入方：@EventListener public void onLogin(LoginSuccessEvent e) { ... }
}

// 模式 D：模板方法钩子（抽象基类留 protected 覆写点）
public abstract class EnhanceMessageHandler<T> {
    protected boolean filter(Message m) { return true; }
    protected abstract void handleMessage(T event);
}
```

四种模式可组合使用。落地时以「接入方最常见的适配诉求」排序，避免为不可能的场景过度抽象（YAGNI）。
