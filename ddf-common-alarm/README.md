# ddf-common-alarm

> Unified alerting and notification module. Aggregates mainstream channels such as DingTalk and Lark,
> supports automatic exception reporting and scheduled-scan alerts, providing a standardized alerting outlet for business systems.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-alarm` solves the **"how to promptly notify ops/developers of system exceptions and critical events"** problem.

| Scenario                    | Typical Problem                                        | What the Module Provides                                |
|-----------------------------|--------------------------------------------------------|---------------------------------------------------------|
| Production exception alerts | Exceptions go unnoticed and incidents escalate         | Automatic exception capture and push to DingTalk / Lark |
| Scheduled job monitoring    | Failed cron jobs trigger no notifications              | Alert on abnormal scan results                          |
| Business-critical events    | Order backlogs, low inventory need immediate attention | Business code proactively calls the alarm API           |
| Multi-channel coverage      | Teams use different IM tools, alerts are scattered     | Unified interface with configurable channels            |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-alarm</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
ddf:
  alarm:
    enabled: true
    ding-talk:
      enabled: true
      webhook: https://oapi.dingtalk.com/robot/send?access_token=xxx
      secret: your-secret                      # Signature key from security settings
    lark:
      enabled: false
      webhook: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      secret: your-secret
    exception:
      enabled: true                            # Enable automatic exception alerting
      interval-seconds: 300                    # Minimum interval for the same exception (anti-spam)
```

---

## 4. Core API

### 4.1 Send alert proactively

```java
@Autowired
private AlarmApi alarmApi;

public void notifyOps() {
    alarmApi.sendAlarm("Order service alert", "Order backlog exceeded 1000, check consumer");
}
```

### 4.2 Automatic exception alerting

With `ddf.alarm.exception.enabled=true`, the module automatically captures unhandled exceptions and pushes alerts without business-code intervention.

Alert content includes: exception type, message, stack trace summary, occurrence time, and application name.

### 4.3 Redis key convention

Alert rate limiting uses Redis caching. Keys are managed uniformly by `AlarmRedisKeyEnum`, following the pattern `{applicationName}:alarm:{type}:{identifier}`.

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom alert channel

Implement `AlarmApi` and register it as a Spring Bean to extend WeChat Work, SMS, or other channels:

```java
@Component
@ConditionalOnProperty(prefix = "ddf.alarm.wechat", name = "enabled", havingValue = "true")
public class WechatAlarmApi implements AlarmApi {
    @Override
    public void sendAlarm(String title, String content) {
        // Call WeChat Work bot API
    }
}
```

### 5.2 Business-layer alerting

Instrument critical business flows with alerts:

```java
@Service
public class OrderService {
    @Autowired
    private AlarmApi alarmApi;

    public void checkBacklog() {
        long backlog = orderMapper.countPending();
        if (backlog > 1000) {
            alarmApi.sendAlarm("Order backlog alert", "Current backlog: " + backlog);
        }
    }
}
```

### 5.3 Alert frequency control

Use `exception.interval-seconds` to control the minimum alert interval for the same exception, preventing message floods during exception storms.

Frequency control is Redis-based and remains effective across distributed deployments.

---

## 6. Interplay with Other Modules

| Module                          | How They Cooperate                                                  |
|---------------------------------|---------------------------------------------------------------------|
| `ddf-common-redis`              | Alert frequency control relies on Redis cache                       |
| `ddf-common-governance-starter` | Mail can serve as a fallback alert channel                          |
| `ddf-common-core`               | Exception summarization, JSON serialization, and other fundamentals |

---

## 7. FAQ

**Q1: Does automatic exception alerting capture all exceptions?**
Only uncaught runtime exceptions. It is recommended to judge whether an alert is needed in your global exception handler.

**Q2: What is alert frequency control based on?**
Redis. The key is generated from the exception class name + method signature hash, sharing the same control window across distributed instances.

**Q3: How do I configure a DingTalk robot?**

1. Add a custom robot in a DingTalk group
2. Copy the Webhook URL and signature key
3. Fill them into `ddf.alarm.ding-talk.webhook` and `ddf.alarm.ding-talk.secret`

**Q4: What happens if no channel is configured?**
`AlarmApi` degrades to a no-op implementation (logs only) and will not block business flow.

---

## 8. References

- Source: `AlarmAutoConfiguration`, `AlarmApi`, `AlarmProperties`
- DingTalk robot docs: https://open.dingtalk.com/document/robots/custom-robot-access
- Lark robot docs: https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN
