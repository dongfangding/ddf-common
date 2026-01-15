# CLAUDE.md

## 模块简介

提供统一的告警通知功能，支持多种告警渠道。

## 核心类

| 类路径                                                 | 功能           |
|-----------------------------------------------------|--------------|
| `com.ddf.boot.common.alarm.api.AlarmApi`            | 告警 API 接口    |
| `com.ddf.boot.common.alarm.config.AlarmProperties`  | 配置属性         |
| `com.ddf.boot.common.alarm.enums.AlarmRedisKeyEnum` | Redis Key 定义 |

## 使用说明

### 1. 配置告警

```yaml
ddf:
  alarm:
    enabled: true
    # 告警相关配置
```

### 2. 发送告警

```java
@Autowired
private AlarmApi alarmApi;

public void sendAlarm(String title, String content) {
    alarmApi.sendAlarm(title, content);
}
```

## 注意事项

1. **渠道配置**：根据实际需求配置告警渠道
2. **频率控制**：避免短时间内发送大量告警
