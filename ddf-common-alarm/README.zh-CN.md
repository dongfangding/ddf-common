# ddf-common-alarm

[English](./README.md) | [中文](./README.zh-CN.md)

告警通知与异常告警模块。

## 当前定位

- 提供告警相关自动配置
- 提供钉钉、飞书等通知能力的基础支撑
- 可结合日志、异常、表扫描等场景使用

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-alarm</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.boot.common.alarm.config.AlarmAutoConfiguration`

## 主要配置类

- `DingTalkProperties`
- `LarkProperties`
- `ExceptionAlarmProperties`

## 说明

- 当前模块更偏向告警能力聚合与通知支撑
- 具体业务告警策略仍建议在业务侧按场景封装
