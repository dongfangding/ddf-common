# ddf-common-alarm 接入指南

> 统一告警通知：以 `AlarmChannel` 渠道策略聚合分发结构化告警，内置钉钉 / Lark 渠道与异常自动告警，支持自定义渠道与频率控制。

## 核心能力

| 能力       | 说明                                        | 关键类 / 入口                                                                    |
|----------|-------------------------------------------|-----------------------------------------------------------------------------|
| 渠道策略 SPI | 渠道抽象，注册 Bean 即被 `List<AlarmChannel>` 聚合分发 | `channel.AlarmChannel`                                                      |
| 钉钉渠道     | 钉钉机器人（markdown 渲染），支持加签                   | `channel.DingTalkAlarmChannel`                                              |
| Lark 渠道  | Lark 机器人（富文本卡片渲染），支持加签                    | `channel.LarkAlarmChannel`                                                  |
| 频率控制     | 静默窗口防告警风暴（默认 Redis 5 分钟）                  | `channel.AlarmFrequencyControl` / `RedisAlarmFrequencyControl`              |
| 结构化消息    | 标题 + 有序内容行，各渠道自行渲染                        | `model.AlarmMessage`                                                        |
| 异常自动告警   | 监听 `GlobalExceptionEvent` 自动按渠道分发         | `notify.CodeExceptionNotify`                                                |
| 配置属性     | 钉钉 / Lark / 异常告警开关                        | `config.DingTalkProperties` / `LarkProperties` / `ExceptionAlarmProperties` |

## 接入方式

### 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-alarm</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 传递依赖 `ddf-common-core`、`ddf-common-redis`、`ddf-common-log4j`（均为 `provided` 作用域，需上层自行引入可用版本）。

告警组件由 `AlarmAutoConfiguration` 通过 `META-INF/spring/...AutoConfiguration.imports` 自动装配，引入依赖后**无需额外注解**即可生效。

### 关键配置

三个配置类的**前缀不一致**，需注意：

```yaml
# 钉钉渠道（DingTalkProperties）
customizer:
  infra:
    alarm:
      dingtalk:
        daily-limit: 100                # 每日发送数量限制
        code-exception:                 # 代码异常告警机器人
          enabled: true
          access-token: "xxx"           # 钉钉机器人 access token
          secret: "xxx"                 # 加签密钥
        biz-resource:                   # 资源告警机器人（可省略）
          enabled: false
        mapping-exception: {}           # 自定义映射机器人配置

# Lark 渠道（LarkProperties，前缀是 customizer.infra.alarm.lark）
customizer:
  infra:
    alarm:
      lark:
        code-exception:
          enabled: true
          webhook-url: "https://open.larksuite.com/open-apis/bot/v2/hook/xxx"
          secret: "xxx"

# 异常告警开关（ExceptionAlarmProperties，前缀是 customizer.infra.alarm.exception）
customizer:
  infra:
    alarm:
      exception:
        enabled: true                     # 是否开启异常告警
        ignore-code-or-message-list: []   # 忽略告警的错误码 / 消息
        ignore-url-list: []               # 忽略告警的接口
```

### 发送告警

业务方直接注入渠道发送结构化消息：

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

## 扩展点

### 1. AlarmChannel 渠道 SPI

实现 `AlarmChannel` 并注册为 Bean，即被 `List<AlarmChannel>` 聚合分发，无需改任何现有代码：

```java
@Component
public class SmsAlarmChannel implements AlarmChannel {

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
        // 按自身能力渲染 message.getTitle() / message.getLines()
    }
}
```

### 2. AlarmFrequencyControl 频率控制（可替换）

默认 `RedisAlarmFrequencyControl` 使用 Redis `SETNX`，对同一 `alarmKey` 5 分钟静默。注册自定义 `AlarmFrequencyControl` Bean（`@ConditionalOnMissingBean`）即可整体替换：

```java
@Component
public class MyAlarmFrequencyControl implements AlarmFrequencyControl {

    @Override
    public boolean tryAcquire(String alarmKey) {
        // 返回 false 表示本次告警被静默跳过
        return true;
    }
}
```

## 注意事项

1. **配置前缀**：统一为 `customizer.infra.alarm.*`（钉钉 `customizer.infra.alarm.dingtalk`、Lark `customizer.infra.alarm.lark`、异常告警开关 `customizer.infra.alarm.exception`）。
2. **频率控制**：默认同一错误码 5 分钟内只告警一次（`AlarmFrequencyControl` 通过 `ObjectProvider` 注入，未提供时跳过频控）。
3. **渠道启用**：`AlarmChannel.isEnabled()` 返回 `false` 的渠道会被 `CodeExceptionNotify` 跳过。
4. **异常来源**：异常自动告警依赖 core 发布的 `GlobalExceptionEvent`，需上层接入统一异常处理后才会有事件。
