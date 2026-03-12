# ddf-common-rocketmq

[English](./README.md) | [中文](./README.zh-CN.md)

RocketMQ 增强集成模块。

## 当前定位

- 基于 `rocketmq-spring-boot-starter` 提供增强封装
- 提供增强版 `RocketProducer`
- 处理 Jackson 对 Java 时间类型的消息转换兼容
- 支持基于属性开关的环境隔离处理

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-rocketmq</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.boot.common.rocketmq.config.RocketMQEnhanceAutoConfiguration`

## 主要类型

- `RocketProducer`
- `RocketEnhanceProperties`
- `EnvironmentIsolationProcessor`

## 相关配置

环境隔离能力开关：

```yaml
rocketmq:
  enhance:
    enabledIsolation: true
```

## 说明

- 当前模块不是对 RocketMQ 全量能力的重写，而是对 Spring RocketMQ 接入的增强层
- 常规 RocketMQ 基础连接参数仍按原生 starter 方式配置
