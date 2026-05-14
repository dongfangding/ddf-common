# ddf-common-governance-starter

> Governance-layer foundation starter. Aggregates Mail, Observability (Actuator + Prometheus), and automatic thread-pool metrics binding.
> Safe to import by default: no errors if mail is unconfigured, silently skips monitoring if not set up.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-governance-starter` solves the **"standardized operations and governance capabilities"** problem.

| Category                  | Typical Problem                                                          | What the Module Provides                                                     |
|---------------------------|--------------------------------------------------------------------------|------------------------------------------------------------------------------|
| Alert emails              | Need to send notification emails on system anomalies                     | `MailService` / `MailUtil` safe sending; doesn't initialize if unconfigured  |
| Thread-pool observability | Custom thread pools have no monitoring for active count or queue backlog | Auto-scan and bind Micrometer metrics to Prometheus                          |
| Health checks             | Spring Boot Actuator endpoints need unified exposure                     | Aggregates `spring-boot-starter-actuator` + `micrometer-registry-prometheus` |
| Governance extensibility  | Future audit, tracing, and alerting need a unified entry point           | Governance prefix `customizer.governance.*`, pluggable capabilities          |

---

## 2. Maven Dependency

Direct dependency:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Or through the default starter (already included):

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

Transitive dependencies:

- `spring-boot-starter-mail`
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`

---

## 3. Minimum Configuration

```yaml
customizer:
  governance:
    mail:
      enabled: true                    # Allow MailService auto-registration (default true)
    observability:
      enabled: true                    # Allow observability auto-registration (default true)
      thread-pool:
        enabled: true                  # Thread pool metrics binding (default true)
        metric-name: custom.thread.pool
        scan-all: false                # false=scan by include rules; true=scan all
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

| Property                                | Description                                            | Default              |
|-----------------------------------------|--------------------------------------------------------|----------------------|
| `mail.enabled`                          | Whether to allow mail service registration             | `true`               |
| `observability.enabled`                 | Whether to allow observability components registration | `true`               |
| `observability.thread-pool.enabled`     | Whether to bind thread pool metrics                    | `true`               |
| `observability.thread-pool.scan-all`    | Whether to scan all thread pool beans                  | `false`              |
| `observability.thread-pool.metric-name` | Micrometer meter name prefix                           | `custom.thread.pool` |

> `mail.enabled=true` does not mean mail will definitely start. `MailService` is only registered when both `spring.mail.*` is configured and the `JavaMailSender` bean exists. Therefore this starter is **safe** to add to all projects.

---

## 4. Core API Guide

### 4.1 Sending emails

#### Inject MailService

```java
@Autowired
private MailService mailService;

public void sendAlert() {
    mailService.sendMimeMail(
        new String[]{"ops@example.com"},     // sendTo
        new String[]{"manager@example.com"}, // cc
        "[ALERT] Order service anomaly",     // subject
        "<h3>Details</h3><p>...</p>",        // content (HTML supported)
        null                                  // attachment
    );
}
```

#### Use MailUtil static utility

```java
// Suitable for scenarios where injection is inconvenient (utility classes, static methods)
MailUtil.sendMimeMail(
    new String[]{"ops@example.com"},
    "Service restart notification",
    "Application restarted at " + LocalDateTime.now()
);
```

> `MailUtil` lazily fetches `MailService` via `SpringContextHolder`, so it doesn't eagerly pull the Bean at class-load time. Even if mail is unconfigured, no error occurs during startup.

#### With attachments

```java
Map<String, File> attachments = Map.of("report.xlsx", new File("/tmp/report.xlsx"));
mailService.sendMimeMail(
    new String[]{"user@example.com"},
    null,
    "Monthly report",
    "Please find the attachment",
    attachments
);
```

Email send failures throw `ServerErrorException(BaseErrorCallbackCode.MAIL_SEND_FAILURE)`, which is uniformly wrapped by the global exception handler.

### 4.2 Thread-pool metrics monitoring

After importing this starter and exposing the Prometheus endpoint, custom thread pools automatically report these metrics:

- `custom_thread_pool_active` — Active thread count
- `custom_thread_pool_completed` — Completed task count
- `custom_thread_pool_queue_remaining` — Queue remaining capacity
- `custom_thread_pool_queued` — Queued task count

Metrics carry `bean`, `threadPrefix`, and `executorType` tags for easy splitting by thread pool in Grafana.

Supported thread pool types:

- `ThreadPoolTaskExecutor`
- `ThreadPoolTaskScheduler`
- `ThreadPoolExecutor`
- `ScheduledThreadPoolExecutor`

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom MailService

Implement `MailService` to integrate with internal email gateways or third-party platforms:

```java
@Component
public class InternalMailService implements MailService {
    @Override
    public void sendMimeMail(String[] sendTo, String[] cc, String subject,
            String content, Map<String, File> attachment) {
        // Call internal corporate email gateway API
        internalGateway.send(new MailRequest(sendTo, subject, content));
    }
}
```

After registration, `DefaultMailService` is no longer active, and `MailUtil` automatically routes to your custom implementation.

### 5.2 Adjust thread-pool scan rules

If business thread pools don't follow naming conventions, expand the scan scope:

```yaml
customizer:
  governance:
    observability:
      thread-pool:
        scan-all: true                   # Scan all ExecutorService type beans
        exclude-bean-name-patterns:
          - "taskScheduler"              # Exclude Spring internal scheduler
          - "applicationTaskExecutor"
```

### 5.3 Actuator endpoint security

In production, don't expose all Actuator endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus       # Only expose health and metrics
      base-path: /admin                  # Non-standard path
  server:
    port: 8081                           # Separate port, not exposed externally
```

---

## 6. Interplay with Other Modules

| Module                       | How They Cooperate                                                                       |
|------------------------------|------------------------------------------------------------------------------------------|
| `ddf-common-core`            | `SpringContextHolder` provides lazy Bean lookup for `MailUtil`                           |
| `ddf-common-api`             | Email send failures throw `ServerErrorException`, caught by the global exception handler |
| `ddf-common-mvc`             | Actuator health checks and Prometheus metrics endpoints are served by the web layer      |
| `ddf-common-starter-default` | This module is included in the default starter                                           |

---

## 7. FAQ

**Q1: Will importing this starter cause errors if `spring.mail.*` is not configured?**  
No. `MailService` registration is guarded by `@ConditionalOnBean(JavaMailSender.class)` and `@ConditionalOnBean(MailProperties.class)`. When mail is unconfigured, these beans don't exist, so `MailService` is not registered and startup proceeds without errors.

**Q2: What happens if `MailUtil.sendMimeMail` is called when mail is not configured?**  
It throws `ServerErrorException(MAIL_SEND_FAILURE)`. If your business needs to silently ignore email unavailability, catch the exception yourself or check for `MailService` existence before injecting.

**Q3: Why can't I see thread-pool metrics in Prometheus?**

1. Confirm `management.endpoints.web.exposure.include` contains `prometheus`
2. Confirm the thread pool bean name matches `include-bean-name-patterns`
3. Confirm the thread pool bean type is in the supported list (see section 4.2)
4. If the custom thread pool is registered via `@Bean`, ensure its return type is a concrete class (e.g. `ThreadPoolExecutor`) rather than the `ExecutorService` interface

**Q4: Where is the email `from` address read from?**  
From `spring.mail.properties.from`. Example:

```yaml
spring:
  mail:
    host: smtp.example.com
    username: noreply@example.com
    password: secret
    properties:
      from: noreply@example.com
```

**Q5: Why is the governance prefix not `customizer.infra`?**  
Governance capabilities (mail, observability, future audit/tracing) are positioned above infrastructure, so they use the independent `customizer.governance` prefix for easier configuration layering and permission management.

---

## 8. References

- Source: `config/GovernanceAutoConfiguration.java`, `config/GovernanceProperties.java`, `config/ThreadPoolMetricsBinder.java`
- Source: `mail/MailService.java`, `mail/DefaultMailService.java`
- Source: `util/MailUtil.java`
