# ddf-common-governance-starter

> 治理层基础 starter。聚合邮件（Mail）、可观测性（Actuator + Prometheus）、线程池指标自动绑定等治理能力。
> 引入后默认安全：未配置邮件时不报错，未配置监控时静默跳过。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-governance-starter` 解决的是 **"运维与治理能力的标准化收口"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| ----- | ----- | ----- |
| 告警邮件 | 系统异常时需要发送通知邮件 | `MailService` / `MailUtil` 安全发送，未配置时不初始化 |
| 线程池可观测 | 自定义线程池的活跃数、队列堆积无监控 | 自动扫描并绑定 Micrometer 指标到 Prometheus |
| 健康检查 | Spring Boot Actuator 需要统一暴露端点 | 聚合 `spring-boot-starter-actuator` + `micrometer-registry-prometheus` |
| 治理层扩展 | 后续审计、链路追踪、告警需要统一入口 | 治理层约定前缀 `customizer.governance.*`，能力可插拔 |

---

## 2. 依赖引入

直接依赖本 starter：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

或通过默认 starter（已包含）：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

传递依赖：

- `spring-boot-starter-mail`
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`

---

## 3. 最小化配置

```yaml
customizer:
  governance:
    mail:
      enabled: true                    # 允许自动注册 MailService（默认 true）
    observability:
      enabled: true                    # 允许自动注册可观测能力（默认 true）
      thread-pool:
        enabled: true                  # 线程池指标绑定（默认 true）
        metric-name: custom.thread.pool
        scan-all: false                # false=按 include 规则扫描；true=扫描所有
        include-bean-name-patterns:
          - "*Executor"
          - "*Pool"
          - "*Scheduler"
        exclude-bean-name-patterns:
          - "applicationTaskExecutor"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

| 属性 | 说明 | 默认值 |
| ----- | ----- | ----- |
| `mail.enabled` | 是否允许注册邮件服务 | `true` |
| `observability.enabled` | 是否允许注册可观测性组件 | `true` |
| `observability.thread-pool.enabled` | 是否绑定线程池指标 | `true` |
| `observability.thread-pool.scan-all` | 是否扫描全部线程池 Bean | `false` |
| `observability.thread-pool.metric-name` | Micrometer meter 名称前缀 | `custom.thread.pool` |

> `mail.enabled=true` 不代表邮件一定启动。只有同时满足 `spring.mail.*` 已配置 + `JavaMailSender` Bean 存在时，`MailService` 才会注册。因此本 starter 可以**安全地**引入到所有项目中。

---

## 4. 核心 API 使用指南

### 4.1 发送邮件

#### 注入 MailService

```java
@Autowired
private MailService mailService;

public void sendAlert() {
    mailService.sendMimeMail(
        new String[]{"ops@example.com"},     // sendTo
        new String[]{"manager@example.com"}, // cc
        "【告警】订单服务异常",                  // subject
        "<h3>异常详情</h3><p>...",            // content（支持 HTML）
        null                                  // attachment
    );
}
```

#### 使用 MailUtil 静态工具

```java
// 适合在不方便注入的场景（如工具类、静态方法）中使用
MailUtil.sendMimeMail(
    new String[]{"ops@example.com"},
    "服务重启通知",
    "应用已于 " + LocalDateTime.now() + " 完成重启"
);
```

> `MailUtil` 底层通过 `SpringContextHolder` 延迟获取 `MailService`，不会在类加载阶段强取 Bean，因此即使邮件未配置也不会报错。

#### 带附件

```java
Map<String, File> attachments = Map.of("report.xlsx", new File("/tmp/report.xlsx"));
mailService.sendMimeMail(
    new String[]{"user@example.com"},
    null,
    "月度报表",
    "请查收附件",
    attachments
);
```

邮件发送失败会抛出 `ServerErrorException(BaseErrorCallbackCode.MAIL_SEND_FAILURE)`，由全局异常处理器统一包装。

### 4.2 线程池指标监控

引入本 starter 并暴露 Prometheus 端点后，自定义线程池会自动上报以下指标：

- `custom_thread_pool_active` — 活跃线程数
- `custom_thread_pool_completed` — 已完成任务数
- `custom_thread_pool_queue_remaining` — 队列剩余容量
- `custom_thread_pool_queued` — 队列中等待的任务数

指标自带 `bean`、`threadPrefix`、`executorType` 标签，便于在 Grafana 中按线程池维度拆分。

支持的线程池类型：

- `ThreadPoolTaskExecutor`
- `ThreadPoolTaskScheduler`
- `ThreadPoolExecutor`
- `ScheduledThreadPoolExecutor`

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义 MailService

实现 `MailService` 接口，用于对接内部邮件网关或第三方邮件平台：

```java
@Component
public class InternalMailService implements MailService {
    @Override
    public void sendMimeMail(String[] sendTo, String[] cc, String subject,
            String content, Map<String, File> attachment) {
        // 调用企业内部邮件网关 API
        internalGateway.send(new MailRequest(sendTo, subject, content));
    }
}
```

注册后 `DefaultMailService` 不再生效，`MailUtil` 也会自动路由到自定义实现。

### 5.2 线程池扫描规则调整

如果业务线程池命名不规范，可以扩大扫描范围：

```yaml
customizer:
  governance:
    observability:
      thread-pool:
        scan-all: true                   # 扫描所有 ExecutorService 类型 Bean
        exclude-bean-name-patterns:
          - "taskScheduler"              # 排除 Spring 内部调度器
          - "applicationTaskExecutor"
```

### 5.3 Actuator 端点安全

生产环境不要暴露所有 Actuator 端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus       # 仅暴露健康和监控
      base-path: /admin                  # 改为非标准路径
  server:
    port: 8081                           # 单独端口，不对外暴露
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| ----- | ----- |
| `ddf-common-core` | `SpringContextHolder` 为 `MailUtil` 提供延迟 Bean 查找 |
| `ddf-common-api` | 邮件发送失败抛出 `ServerErrorException`，由全局异常处理器统一包装 |
| `ddf-common-mvc` | Actuator 健康检查、Prometheus 指标端点由 Web 层承载 |
| `ddf-common-starter-default` | 默认 starter 已包含本模块 |

---

## 7. FAQ

**Q1：没有配置 `spring.mail.*`，引入本 starter 会报错吗？**  
不会。`MailService` 的注册被 `@ConditionalOnBean(JavaMailSender.class)` 和 `@ConditionalOnBean(MailProperties.class)` 保护。未配置邮件时，这些 Bean 不存在，`MailService` 不会注册，启动无异常。

**Q2：`MailUtil.sendMimeMail` 在邮件未配置时调用会怎样？**  
会抛出 `ServerErrorException(MAIL_SEND_FAILURE)`。若业务希望在邮件不可用时静默忽略，请自行捕获异常或注入 `MailService` 前做存在性检查。

**Q3：线程池指标为什么在 Prometheus 中看不到？**  
1. 确认 `management.endpoints.web.exposure.include` 包含 `prometheus`
2. 确认线程池 Bean 名称匹配 `include-bean-name-patterns`
3. 确认线程池 Bean 的类型在支持列表内（见 4.2 节）
4. 若自定义线程池通过 `@Bean` 注册，确保其返回类型是具体类（如 `ThreadPoolExecutor`）而非 `ExecutorService` 接口

**Q4：邮件的 `from` 地址从哪里读取？**  
从 `spring.mail.properties.from` 读取。示例：
```yaml
spring:
  mail:
    host: smtp.example.com
    username: noreply@example.com
    password: secret
    properties:
      from: noreply@example.com
```

**Q5： governance 前缀为什么不是 `customizer.infra`？**  
治理层能力（mail、observability、未来审计/链路追踪）定位高于基础设施，因此使用独立的 `customizer.governance` 前缀，便于权限和配置分层管理。

---

## 8. 参考

- 源码：`config/GovernanceAutoConfiguration.java`、`config/GovernanceProperties.java`、`config/ThreadPoolMetricsBinder.java`
- 源码：`mail/MailService.java`、`mail/DefaultMailService.java`
- 源码：`util/MailUtil.java`
