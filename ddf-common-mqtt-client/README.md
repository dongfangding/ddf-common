# ddf-common-mqtt-client

> Higher-level business wrapper module built on top of `ddf-common-mqtt`. Provides business-oriented Topic definitions,
> message body abstractions, and REST controllers for rapid MQTT publishing integration.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-mqtt-client` solves the **"how to quickly and standardize MQTT message publishing at the business layer"** problem.

| Scenario                                | Typical Problem                                        | What the Module Provides                                    |
|-----------------------------------------|--------------------------------------------------------|-------------------------------------------------------------|
| Business system pushing device commands | Don't want to touch low-level MQTT APIs directly       | Controller wrapper — HTTP call triggers publish             |
| Multi-business-line Topic management    | Topic naming is chaotic and hard to maintain           | `MqttTopicDefine` enumeration for centralized management    |
| Structured message bodies               | Passing raw JSON strings is error-prone                | `TextMessageBody` and other business message wrappers       |
| Integrate with existing web services    | Already have Spring MVC endpoints, want to extend MQTT | Auto-injected `MqttClientController` exposes REST endpoints |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt-client</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> This module transitively depends on `ddf-common-mqtt`; no need to add it separately.

---

## 3. Minimum Configuration

Connection settings reuse `ddf-common-mqtt` configuration. The business layer only needs to define Topics and message bodies:

```yaml
customizer:
  infra:
    mqtt:
      enable: true
      server-host: localhost
      server-port: 1883
      client-id: "ddf-client-001"
      username: admin
      password: password
```

---

## 4. Core API

### 4.1 Define Business Topics

Use `MqttTopicDefine` enum for unified Topic management:

```java
public enum BizMqttTopic implements MqttTopicDefine {
    DEVICE_COMMAND("device/{deviceId}/command"),
    DEVICE_STATUS("device/{deviceId}/status"),
    ALERT_NOTIFICATION("alert/notification");

    private final String topic;

    BizMqttTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String getTopic() {
        return topic;
    }
}
```

### 4.2 Build Business Message Body

```java
TextMessageBody message = TextMessageBody.builder()
    .topic(BizMqttTopic.DEVICE_COMMAND.getTopic())
    .content("{\"action\":\"reboot\",\"delay\":0}")
    .build();
```

### 4.3 Publish via Controller (REST Call)

The module auto-exposes `MqttClientController`, supporting MQTT publish triggered by HTTP requests:

```java
@Autowired
private MqttClientController mqttClientController;

// Send device reboot command
MqttMessageRequest request = MqttMessageRequest.builder()
    .topic("device/001/command")
    .payLoad("{\"action\":\"reboot\"}")
    .qos(1)
    .async(true)
    .build();

ResponseData<?> response = mqttClientController.publish(request);
```

### 4.4 Directly Use Underlying Client

For more flexible control, inject the base module's `MqttPublishClient`:

```java
@Autowired
private MqttPublishClient mqttClient;

ResponseData<MqttMessageResponse> response = mqttClient.publish(
    InnerMqttMessageRequest.builder()
        .topic(BizMqttTopic.ALERT_NOTIFICATION.getTopic())
        .payLoad(alertData)
        .control(MqttMessageControl.builder()
            .async(false)
            .qos(MqttQosEnum.AT_LAST_ONCE)
            .build())
        .build()
);
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom Message Body Types

Extend the module's message body interface for business-specific formats:

```java
@Data
@Builder
public class DeviceCommandBody implements MqttMessageBody {
    private String deviceId;
    private String action;
    private Map<String, Object> params;

    @Override
    public String toPayload() {
        return JsonUtil.toJson(this);
    }
}
```

### 5.2 Topic Template Parameter Substitution

Supports `{variable}` placeholder replacement:

```java
String topic = BizMqttTopic.DEVICE_COMMAND.getTopic()
    .replace("{deviceId}", "DEV-001");
```

---

## 6. Interplay with Other Modules

| Module            | How They Cooperate                                                                             |
|-------------------|------------------------------------------------------------------------------------------------|
| `ddf-common-mqtt` | Underlying MQTT connection and publishing capability; this module adds business-layer wrapping |
| `ddf-common-core` | JSON serialization and utility support                                                         |
| `ddf-common-mvc`  | REST controller exposure and unified response format                                           |

---

## 7. FAQ

**Q1: What's the difference between this module and `ddf-common-mqtt`?**
`ddf-common-mqtt` is protocol-layer (connection, publish, QoS); this module is business-layer (Topic definitions, message body models, REST controllers). They are layered vertically.

**Q2: Does this module support MQTT subscription/consumption?**
The default implementation focuses on the publishing flow. Subscription/consumption should be implemented in the business layer using the Paho client directly, or by extending `ddf-common-mqtt` subscription capabilities.

**Q3: Should the controller endpoints be access-controlled?**
In production, it is recommended to add authentication or restrict `MqttClientController` endpoints to internal networks to prevent unauthorized message publishing.

---

## 8. References

- Source: `MqttClientController`, `MqttMessageRequest`, `MqttTopicDefine`, `TextMessageBody`
- Dependency module: `ddf-common-mqtt`
