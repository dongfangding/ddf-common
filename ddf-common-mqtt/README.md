# ddf-common-mqtt

> MQTT client integration module. Based on Eclipse Paho MQTT v5, provides message publishing capabilities with sync/async sending,
> multiple QoS levels, and SSL encryption. Suitable for IoT device communication and message push scenarios.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-mqtt` solves the **"application publishing messages to an MQTT broker"** problem.

| Scenario                    | Typical Problem                                           | What the Module Provides                        |
|-----------------------------|-----------------------------------------------------------|-------------------------------------------------|
| IoT device command dispatch | Need to push control commands to large numbers of devices | Async publishing with QoS 1 guaranteed delivery |
| Real-time status reporting  | Sensor data needs high-throughput push                    | QoS 0 async send for best performance           |
| Message push gateway        | Need to connect to various MQTT brokers                   | Unified client wrapper, connect with config     |
| Secure transport            | Public network transport needs anti-eavesdropping         | SSL/TLS encrypted connections                   |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

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
      clean-start: true
      keep-alive-interval: 60
      connection-timeout: 30
      ssl-enabled: false
```

---

## 4. Core API

### 4.1 Sync send (QoS 1)

```java
@Autowired
private MqttPublishClient mqttClient;

ResponseData<MqttMessageResponse> response = mqttClient.publish(
    InnerMqttMessageRequest.builder()
        .topic("device/data")
        .payLoad(sensorData)
        .control(MqttMessageControl.builder()
            .async(false)
            .qos(MqttQosEnum.AT_LAST_ONCE)  // QoS 1
            .retain(false)
            .build())
        .build()
);
```

### 4.2 Async send (QoS 0)

```java
mqttClient.publish(
    InnerMqttMessageRequest.builder()
        .topic("device/telemetry")
        .payLoad(telemetryData)
        .control(MqttMessageControl.builder()
            .async(true)
            .qos(MqttQosEnum.AT_MOST_ONCE)  // QoS 0
            .build())
        .build()
);
```

### 4.3 QoS levels

| Level | Name           | Description                         | Suitable For                    |
|-------|----------------|-------------------------------------|---------------------------------|
| 0     | `AT_MOST_ONCE` | At most once, no delivery guarantee | Logs, status updates            |
| 1     | `AT_LAST_ONCE` | At least once, possible duplicates  | Command dispatch, notifications |
| 2     | `EXACTLY_ONCE` | Exactly once, no duplicates         | Financial transactions          |

---

## 5. Advanced Usage / Extension Points

### 5.1 Independent thread pools per QoS

```java
@Bean(name = "qos0Executors")
public ThreadPoolTaskExecutor qos0Executor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("mqtt-qos0-");
    executor.initialize();
    return executor;
}
```

### 5.2 SSL encrypted connection

```yaml
customizer:
  infra:
    mqtt:
      ssl-enabled: true
      server-port: 8883
```

---

## 6. Interplay with Other Modules

| Module                   | How They Cooperate                                                                                                    |
|--------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `ddf-common-mqtt-client` | Higher-level business wrapper on top of this module, providing closer-to-business Topic and message body abstractions |
| `ddf-common-core`        | JSON serialization, thread pools, and other fundamentals                                                              |

---

## 7. FAQ

**Q1: Does this module include subscription/consumption capabilities?**
The default implementation focuses on the message publishing flow. For a fuller subscription and consumption model, use `ddf-common-mqtt-client` or extend on top of the current base.

**Q2: What happens if async send fails?**
Async send failures are only logged; there is no built-in retry mechanism. For important messages, use sync sending or implement business-layer compensation.

**Q3: Will the client auto-reconnect after disconnection?**
The Paho client has built-in auto-reconnect. Specific behavior depends on `clean-start` and session persistence settings.

---

## 8. References

- Source: `MqttPublishClient`, `DefaultMqttPublishImpl`, `EmqConnectionProperties`
- Eclipse Paho docs: https://www.eclipse.org/paho/
