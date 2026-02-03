# ddf-common-rocketmq

RocketMQ 消息队列模块。

## 功能特性

- 消息发送
- 消息消费
- 顺序消息
- 事务消息

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-rocketmq</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径                | 功能   |
|--------------------|------|
| `RocketMqProducer` | 生产者  |
| `RocketMqConsumer` | 消费者  |
| `MqProperties`     | 配置属性 |

## 使用说明

### 配置

```yaml
ddf:
  rocketmq:
    producer:
      namesrv-addr: localhost:9876
    consumer:
      namesrv-addr: localhost:9876
```

### 发送消息

```java
@Autowired
private RocketMqProducer producer;

public void send(String topic, String message) {
    producer.send(topic, message);
}
```
