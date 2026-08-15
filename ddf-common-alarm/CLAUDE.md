# CLAUDE.md

## 模块简介

提供统一的告警通知功能，支持多种告警渠道。

## 核心类

| 类路径                                                           | 功能                    |
|---------------------------------------------------------------|-----------------------|
| `com.ddf.boot.common.alarm.channel.AlarmChannel`              | 告警渠道策略接口             |
| `com.ddf.boot.common.alarm.channel.DingTalkAlarmChannel`      | 钉钉渠道实现                |
| `com.ddf.boot.common.alarm.channel.LarkAlarmChannel`          | Lark 渠道实现              |
| `com.ddf.boot.common.alarm.channel.AlarmFrequencyControl`     | 告警频率控制接口             |
| `com.ddf.boot.common.alarm.channel.RedisAlarmFrequencyControl` | 默认频率控制（Redis 5 分钟静默） |
| `com.ddf.boot.common.alarm.model.AlarmMessage`                | 结构化告警消息（title + lines） |
| `com.ddf.boot.common.alarm.config.ExceptionAlarmProperties`   | 异常告警配置属性             |
| `com.ddf.boot.common.alarm.enums.AlarmRedisKeyEnum`           | Redis Key 定义           |

## 使用说明

### 1. 配置告警渠道

```yaml
customizer:
  infra:
    alarm:
      dingtalk:
        code-exception:
          enabled: true
          access-token: "xxx"     # 钉钉机器人 access token
          secret: "xxx"           # 加签密钥

customs:
  alarm:
    exception:
      enabled: true                        # 是否开启异常告警
      ignore-code-or-message-list: []      # 忽略告警的错误码/消息
    lark:
      code-exception:
        enabled: true
        webhook-url: "https://open.larksuite.com/open-apis/bot/v2/hook/xxx"
        secret: "xxx"
```

### 2. 发送告警

内置 `CodeExceptionNotify` 监听 `GlobalExceptionEvent` 自动按渠道分发告警。业务方也可直接注入渠道发送结构化消息：

```java
@Autowired
private List<AlarmChannel> alarmChannels;

public void sendAlarm(String title, List<String> lines) {
    AlarmMessage message = new AlarmMessage(title, lines);
    for (AlarmChannel channel : alarmChannels) {
        if (channel.isEnabled()) {
            channel.send(message);
        }
    }
}
```

### 3. 自定义告警渠道

接入方注册自定义 `AlarmChannel` Bean，即被 `List<AlarmChannel>` 聚合分发：

```java
@Component
public class MyAlarmChannel implements AlarmChannel {

    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void send(AlarmMessage message) {
        // 发送短信
    }
}
```

### 4. 自定义频率控制

默认 `RedisAlarmFrequencyControl`（同一 `alarmKey` 5 分钟内静默），接入方注册 `AlarmFrequencyControl` 类型 Bean 替换：

```java
@Component
public class MyAlarmFrequencyControl implements AlarmFrequencyControl {

    @Override
    public boolean tryAcquire(String alarmKey) {
        // 自定义静默窗口逻辑，返回 false 表示跳过本次告警
        return true;
    }
}
```

## 注意事项

1. **渠道配置**：统一前缀 `customizer.infra.alarm.*`（钉钉 `customizer.infra.alarm.dingtalk`、Lark `customizer.infra.alarm.lark`、异常告警开关 `customizer.infra.alarm.exception`）
2. **频率控制**：默认同一错误码 5 分钟内只告警一次，避免告警风暴
3. **渠道启用**：`AlarmChannel.isEnabled()` 返回 false 时该渠道会被跳过
