# ddf-common-alarm

[English](./README.md) | [中文](./README.zh-CN.md)

Alert notification and exception alerting module.

## Current Positioning

- Provides alarm-related auto-configuration
- Provides baseline support for DingTalk, Lark, and similar notification channels
- Can be combined with logs, exceptions, table scan alerts, and similar scenarios

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-alarm</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.boot.common.alarm.config.AlarmAutoConfiguration`

## Main Configuration Types

- `DingTalkProperties`
- `LarkProperties`
- `ExceptionAlarmProperties`

## Notes

- The module currently focuses on alarm capability aggregation and notification support
- Concrete business alerting strategies should still be packaged on the business side by scenario
