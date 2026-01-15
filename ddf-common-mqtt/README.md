# ddf-common-mqtt

MQTT 协议模块，提供 MQTT 服务端功能。

## 功能特性

- MQTT 消息接收
- 主题订阅管理
- 消息转发

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-mqtt</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `MqttServerApi` | MQTT 服务端 API |
| `MqttProperties` | 配置属性 |

## 使用说明

```yaml
ddf:
  mqtt:
    port: 1883
    websocket-port: 8080
```
