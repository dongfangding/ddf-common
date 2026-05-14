# ddf-common-mqtt

> MQTT 客户端集成模块。基于 Eclipse Paho MQTT v5 提供消息发布能力，支持同步/异步发送、多 QoS 等级和 SSL 加密，
> 适用于物联网设备通信、消息推送等场景。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-mqtt` 解决的是 **"应用向 MQTT Broker 发布消息"** 问题。

| 场景        | 典型问题               | 模块提供的能力         |
|-----------|--------------------|-----------------|
| 物联网设备指令下发 | 需要向大量设备推送控制指令      | 异步发布，QoS 1 保证送达 |
| 实时状态上报    | 传感器数据需要高吞吐推送       | QoS 0 异步发送，性能最优 |
| 消息推送网关    | 需要连接多种 MQTT Broker | 统一客户端封装，配置即连    |
| 安全传输      | 公网传输需防窃听           | SSL/TLS 加密连接    |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mqtt</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

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

## 4. 核心 API

### 4.1 同步发送（QoS 1）

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

### 4.2 异步发送（QoS 0）

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

### 4.3 QoS 等级说明

| 等级 | 名称             | 说明         | 适用场景    |
|----|----------------|------------|---------|
| 0  | `AT_MOST_ONCE` | 最多一次，不保证送达 | 日志、状态更新 |
| 1  | `AT_LAST_ONCE` | 最少一次，可能重复  | 指令下发、通知 |
| 2  | `EXACTLY_ONCE` | 恰好一次，保证不重复 | 金融交易    |

---

## 5. 进阶用法 / 扩展点

### 5.1 按 QoS 配置独立线程池

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

### 5.2 SSL 加密连接

```yaml
customizer:
  infra:
    mqtt:
      ssl-enabled: true
      server-port: 8883
```

---

## 6. 与其他模块协作

| 模块                       | 协作方式                             |
|--------------------------|----------------------------------|
| `ddf-common-mqtt-client` | 本模块的上层业务封装，提供更贴近业务的 Topic 和消息体抽象 |
| `ddf-common-core`        | JSON 序列化、线程池等基础支撑                |

---

## 7. FAQ

**Q1：本模块是否包含订阅消费能力？**
当前默认实现重点在消息发布链路。如需完整的订阅消费模型，建议使用 `ddf-common-mqtt-client` 或在现有基础上扩展。

**Q2：异步发送失败会怎样？**
异步发送失败仅记录日志，没有内置重试机制。重要消息建议使用同步发送或业务层补偿。

**Q3：客户端断连后会自动重连吗？**
Paho 客户端内置自动重连机制，具体行为取决于 `clean-start` 和会话保留设置。

---

## 8. 参考

- 源码：`MqttPublishClient`、`DefaultMqttPublishImpl`、`EmqConnectionProperties`
- Eclipse Paho 文档：https://www.eclipse.org/paho/
