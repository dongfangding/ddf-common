# ddf-common-mqtt-client

MQTT 客户端模块，提供 MQTT 协议客户端功能。

## 功能特性

- MQTT 连接管理
- 消息发布/订阅
- 自动重连

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt-client</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `com.ddf.boot.common.mqtt.client.config.MqttClientProperties` | 客户端配置 |
| `com.ddf.boot.common.mqtt.client.api.MqttClientApi` | MQTT 客户端 API |

## 使用说明

### 配置

```yaml
ddf:
  mqtt:
    client:
      server-uri: tcp://localhost:1883
      client-id: client-001
      username: guest
      password: guest
```

### 发布消息

```java
@Autowired
private MqttClientApi mqttClientApi;

public void publish(String topic, String payload) {
    mqttClientApi.publish(topic, payload);
}
```

## 注意事项

1. **连接管理**：确保 MQTT Broker 可用
2. **消息 QoS**：根据业务需求选择合适的 QoS 等级
