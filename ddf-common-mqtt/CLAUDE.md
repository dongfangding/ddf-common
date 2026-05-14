# CLAUDE.md

## 模块简介

提供 MQTT 协议集成，支持消息发布、订阅和 EMQ X 集成。

## 核心类

| 类路径                                                                  | 功能         |
|----------------------------------------------------------------------|------------|
| `com.ddf.common.boot.mqtt.client.MqttPublishClient`                  | MQTT 发布客户端 |
| `com.ddf.common.boot.mqtt.client.DefaultMqttPublishImpl`             | 发布实现       |
| `com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties` | 连接配置       |

## 使用说明

### 1. 配置

```yaml
ddf:
  mqtt:
    server-host: localhost              # MQTT 服务器地址
    server-port: 1883                   # 端口
    client-id: "ddf-client-001"         # 客户端 ID
    username: admin                     # 用户名
    password: password                  # 密码
    clean-start: true                   # 清除会话
    keep-alive-interval: 60             # 心跳间隔（秒）
    connection-timeout: 30              # 连接超时（秒）
    ssl-enabled: false                  # 是否启用 SSL
```

### 2. 发布消息

```java
@Autowired
private MqttPublishClient mqttClient;

// 同步发送（QoS 1）
ResponseData<MqttMessageResponse> response = mqttClient.publish(
    InnerMqttMessageRequest.builder()
        .topic("device/data")           // 主题
        .payLoad(data)                  // 消息内容
        .control(MqttMessageControl.builder()
            .async(false)               // 同步发送
            .qos(MqttQosEnum.AT_LAST_ONCE)  // QoS 1: 最少一次
            .retain(false)              // 不保留消息
            .build())
        .build()
);

// 异步发送（QoS 0）
ResponseData<MqttMessageResponse> response = mqttClient.publish(
    InnerMqttMessageRequest.builder()
        .topic("device/telemetry")
        .payLoad(telemetryData)
        .control(MqttMessageControl.builder()
            .async(true)                // 异步发送
            .qos(MqttQosEnum.AT_MOST_ONCE)  // QoS 0: 最多一次
            .retain(false)
            .build())
        .build()
);
```

### 3. QoS 等级

| 等级 | 名称             | 说明         |
|----|----------------|------------|
| 0  | `AT_MOST_ONCE` | 最多一次，不保证送达 |
| 1  | `AT_LAST_ONCE` | 最少一次，可能重复  |
| 2  | `EXACTLY_ONCE` | 恰好一次，保证不重复 |

### 4. 配置线程池

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

@Bean(name = "qos1Executors")
public ThreadPoolTaskExecutor qos1Executor() {
    // QoS 1 线程池配置
}

@Bean(name = "qos2Executors")
public ThreadPoolTaskExecutor qos2Executor() {
    // QoS 2 线程池配置
}
```

## 注意事项

1. **QoS 选择**：根据业务需求选择合适的 QoS 等级
    - QoS 0：日志、状态更新等丢失可容忍的场景
    - QoS 1：重要通知、命令下发等
    - QoS 2：金融交易等必须保证不重复的场景
2. **异步发送**：异步发送失败只记录日志，没有重试机制
3. **线程池**：不同 QoS 等级可配置独立线程池
4. **SSL 配置**：生产环境建议启用 SSL 加密
