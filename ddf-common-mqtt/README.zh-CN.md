# ddf-common-mqtt

[English](./README.md) | [中文](./README.zh-CN.md)

MQTT 客户端集成模块。

## 当前定位

- 基于 Eclipse Paho MQTT v5 提供客户端连接能力
- 提供消息发送、重试、线程池和连接配置支撑
- 当前代码更偏“客户端发送能力封装”，不是独立 MQTT Broker 服务端实现

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.common.boot.mqtt.config.MqttAutoConfiguration`

## 主要类型

- `MqttPublishClient`
- `DefaultMqttPublishImpl`
- `EmqConnectionProperties`

## 配置前缀

```yaml
customizer:
  infra:
    mqtt:
      enable: true
```

## 说明

- 当前默认实现重点在消息发布链路
- 如需更完整的订阅消费模型，建议在现有基础上继续向上扩展
