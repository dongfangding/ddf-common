# ddf-common-alarm

告警通知模块，提供统一的告警发送功能。

## 功能特性

- 多渠道告警支持
- 告警模板配置
- 告警分级处理

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-alarm</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径                                                | 功能        |
|----------------------------------------------------|-----------|
| `com.ddf.boot.common.alarm.api.AlarmApi`           | 告警 API 接口 |
| `com.ddf.boot.common.alarm.config.AlarmProperties` | 配置属性      |

## 使用说明

### 配置

```yaml
ddf:
  alarm:
    enabled: true
    # 告警配置
```

### 发送告警

```java
@Autowired
private AlarmApi alarmApi;

public void sendAlarm(String title, String content) {
    alarmApi.sendAlarm(title, content);
}
```
