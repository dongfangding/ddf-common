# ddf-common-mqtt-client

[English](./README.md) | [中文](./README.zh-CN.md)

Higher-level MQTT client model module built on top of `ddf-common-mqtt`.

## Current Positioning

- Provides business-oriented message body and topic abstractions
- Provides controller and model support for MQTT client scenarios
- Depends on the base `ddf-common-mqtt` module

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt-client</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.common.boot.mqttclient.config.MqttClientAutoConfiguration`

## Main Types

- `MqttClientController`
- `MqttMessageRequest`
- `MqttTopicDefine`
- `TextMessageBody`

## Notes

- This is not an independent MQTT protocol implementation, but a business-facing wrapper around the base MQTT client capability
