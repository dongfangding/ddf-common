# ddf-common-mqtt

[English](./README.md) | [中文](./README.zh-CN.md)

MQTT client integration module.

## Current Positioning

- Provides client connectivity based on Eclipse Paho MQTT v5
- Provides publishing, retry, thread-pool, and connection configuration support
- The current implementation is focused on client-side publishing support rather than a standalone MQTT broker implementation

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.common.boot.mqtt.config.MqttAutoConfiguration`

## Main Types

- `MqttPublishClient`
- `DefaultMqttPublishImpl`
- `EmqConnectionProperties`

## Configuration Prefix

```yaml
customizer:
  infra:
    mqtt:
      enable: true
```

## Notes

- The default implementation is centered on the publishing flow
- If a fuller subscription and consumer model is needed, it should be extended on top of the current base
