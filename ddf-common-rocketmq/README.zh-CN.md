# ddf-common-rocketmq

> RocketMQ 增强集成模块。基于 `rocketmq-spring-boot-starter` 提供增强版生产者、消息转换兼容和环境隔离能力，
> 是对原生 Spring RocketMQ 接入的轻量增强层。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-rocketmq` 解决的是 **"原生 RocketMQ Spring 集成不够用，需要增强封装"** 问题。

| 场景             | 典型问题                                      | 模块提供的能力                                       |
|----------------|-------------------------------------------|-----------------------------------------------|
| 统一消息发送入口       | 原生 `RocketMQTemplate` API 较底层，业务代码重复      | `RocketProducer` 统一封装同步/异步/延迟发送               |
| Java 8 时间类型序列化 | Jackson 序列化 `LocalDateTime` 等类型时消费端反序列化失败 | 内置消息转换器，兼容 Java 8 日期时间类型                      |
| 多环境消息隔离        | 开发/测试环境消息串到生产 Topic                       | `EnvironmentIsolationProcessor` 按环境前缀隔离 Topic |
| 消费者增强基类        | 消费者需重复写过滤、重试、异常处理逻辑                       | `EnhanceMessageHandler` 基类统一封装                |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-rocketmq</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 本模块已传递依赖 `rocketmq-spring-boot-starter`，无需重复引入。

---

## 3. 最小化配置

原生 RocketMQ 连接参数仍按 starter 方式配置：

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
```

模块增强配置：

```yaml
rocketmq:
  enhance:
    enabled-isolation: true   # 启用环境隔离
```

---

## 4. 核心 API

### 4.1 同步发送

```java
@Autowired
private RocketProducer rocketProducer;

RocketMqMessage message = RocketMqMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-created")
    .payLoad(orderData)
    .build();

rocketProducer.syncSend(message);
```

### 4.2 发送延迟消息

```java
RocketMqMessage delayMessage = RocketMqMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-timeout")
    .payLoad(orderData)
    .delayTime(3000)  // 延迟 3 秒投递
    .build();

rocketProducer.syncSend(delayMessage);
```

### 4.3 异步发送

```java
rocketProducer.asyncSend(RocketMqMessage.builder()
    .topic("NOTIFY_TOPIC")
    .tag("sms")
    .payLoad(smsData)
    .build(), new SendCallback() {
        @Override
        public void onSuccess(SendResult sendResult) {
            log.info("发送成功: {}", sendResult.getMsgId());
        }

        @Override
        public void onException(Throwable e) {
            log.error("发送失败", e);
        }
    });
```

### 4.4 消费消息（增强基类）

```java
@Component
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",
    consumerGroup = "order-consumer-group"
)
public class OrderConsumer extends EnhanceMessageHandler<OrderCreatedEvent> {

    @Override
    public boolean filter(Message message) {
        // 按 Tag 过滤消息
        return "order-created".equals(message.getTag());
    }

    @Override
    public void handleMessage(OrderCreatedEvent event) {
        orderService.onOrderCreated(event);
    }
}
```

### 4.5 批量消费

```java
@Component
@RocketMQMessageListener(
    topic = "BATCH_TOPIC",
    consumerGroup = "batch-consumer-group"
)
public class BatchConsumer extends EnhanceBatchMessageHandler<OrderEvent> {

    @Override
    public void handleMessage(List<OrderEvent> events) {
        orderService.batchProcess(events);
    }
}
```

---

## 5. 进阶用法 / 扩展点

### 5.1 环境隔离原理

启用 `enabled-isolation: true` 后，模块会自动为 Topic 添加环境前缀：

| 环境   | 原始 Topic      | 实际 Topic           |
|------|---------------|--------------------|
| dev  | `ORDER_TOPIC` | `DEV_ORDER_TOPIC`  |
| test | `ORDER_TOPIC` | `TEST_ORDER_TOPIC` |
| prod | `ORDER_TOPIC` | `ORDER_TOPIC`（不变）  |

避免开发/测试消息污染生产环境。

### 5.2 消息体格式

```java
RocketMqMessage message = RocketMqMessage.builder()
    .topic("TOPIC")           // 主题
    .tag("TAG")               // 标签，用于消费者过滤
    .payLoad(Object)          // 消息内容（任意对象，自动 JSON 序列化）
    .key("KEY")               // 业务 Key，用于查询和去重
    .delayTime(Long)          // 延迟时间（毫秒）
    .build();
```

### 5.3 顺序消息

如需发送顺序消息，使用 `MessageQueueSelector` 确保同一业务 Key 的消息进入同一队列：

```java
rocketProducer.syncSendOrderly(message, orderId);
```

---

## 6. 与其他模块协作

| 模块                          | 协作方式                                   |
|-----------------------------|----------------------------------------|
| `ddf-common-ons`            | 如从 ONS 迁移到自建 RocketMQ，消费者抽象模式类似，降低迁移成本 |
| `ddf-common-core`           | JSON 序列化、工具类支撑                         |
| `ddf-common-authentication` | 消息体中如需携带用户上下文，可结合认证模块的 Token 机制        |

---

## 7. FAQ

**Q1：本模块与 `rocketmq-spring-boot-starter` 是什么关系？**
本模块是对原生 starter 的增强层，不是重写。所有原生配置和注解仍然有效，模块在其之上补充了统一生产者、Java 8 时间兼容、环境隔离等能力。

**Q2：环境隔离会影响已有的 Topic 吗？**
不会。`EnvironmentIsolationProcessor` 只在发送时动态修改 Topic 名，消费端同样会按当前环境前缀订阅，两边保持一致。

**Q3：事务消息是否支持？**
RocketMQ 原生支持事务消息，但本模块未做额外封装。如需使用，建议直接调用 `RocketMQTemplate` 的事务发送 API。

**Q4：消费者中的 `filter()` 方法有什么作用？**
`EnhanceMessageHandler` 的 `filter()` 用于在 `handleMessage()` 之前做消息过滤，返回 `false` 的消息会被跳过，避免无效消息进入业务逻辑。

---

## 8. 参考

- 源码：`RocketProducer`、`RocketEnhanceProperties`、`EnvironmentIsolationProcessor`、`EnhanceMessageHandler`
- RocketMQ Spring 文档：https://github.com/apache/rocketmq-spring
