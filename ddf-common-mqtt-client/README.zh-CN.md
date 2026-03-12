# ddf-common-mqtt-client

[English](./README.md) | [中文](./README.zh-CN.md)

基于 `ddf-common-mqtt` 的上层 MQTT 客户端模型封装模块。

## 当前定位

- 提供更贴近业务的消息体与 Topic 抽象
- 提供 MQTT client 相关控制器与模型支撑
- 依赖基础模块 `ddf-common-mqtt`

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt-client</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.common.boot.mqttclient.config.MqttClientAutoConfiguration`

## 主要类型

- `MqttClientController`
- `MqttMessageRequest`
- `MqttTopicDefine`
- `TextMessageBody`

## 说明

- 该模块不是独立 MQTT 协议实现，而是对基础 MQTT 客户端能力的业务化封装
