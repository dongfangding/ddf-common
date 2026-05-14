# ddf-common-rocketmq

> RocketMQ enhancement integration module. Built on `rocketmq-spring-boot-starter`, provides an enhanced producer,
> message conversion compatibility for Java time types, and environment isolation capabilities.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-rocketmq` solves the **"native RocketMQ Spring integration is insufficient and needs an enhancement layer"** problem.

| Scenario                            | Typical Problem                                                            | What the Module Provides                                          |
|-------------------------------------|----------------------------------------------------------------------------|-------------------------------------------------------------------|
| Unified message sending entry       | Native `RocketMQTemplate` API is low-level; business code is repetitive    | `RocketProducer` unifies sync/async/delayed sending               |
| Java 8 time type serialization      | Jackson serialization of `LocalDateTime` fails on consumer deserialization | Built-in message converter compatible with Java 8 date/time types |
| Multi-environment message isolation | Dev/test messages leak into production Topics                              | `EnvironmentIsolationProcessor` prefixes Topics by environment    |
| Consumer enhancement base class     | Consumers repeatedly write filtering, retry, and exception handling        | `EnhanceMessageHandler` base class encapsulates common logic      |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-rocketmq</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> This module transitively depends on `rocketmq-spring-boot-starter`; no need to add it separately.

---

## 3. Minimum Configuration

Native RocketMQ connection parameters are still configured via the starter:

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
```

Module enhancement configuration:

```yaml
rocketmq:
  enhance:
    enabled-isolation: true   # Enable environment isolation
```

---

## 4. Core API

### 4.1 Sync Send

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

### 4.2 Send Delayed Message

```java
RocketMqMessage delayMessage = RocketMqMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-timeout")
    .payLoad(orderData)
    .delayTime(3000)  // Delay 3 seconds
    .build();

rocketProducer.syncSend(delayMessage);
```

### 4.3 Async Send

```java
rocketProducer.asyncSend(RocketMqMessage.builder()
    .topic("NOTIFY_TOPIC")
    .tag("sms")
    .payLoad(smsData)
    .build(), new SendCallback() {
        @Override
        public void onSuccess(SendResult sendResult) {
            log.info("Send success: {}", sendResult.getMsgId());
        }

        @Override
        public void onException(Throwable e) {
            log.error("Send failed", e);
        }
    });
```

### 4.4 Consume Message (Enhanced Base Class)

```java
@Component
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",
    consumerGroup = "order-consumer-group"
)
public class OrderConsumer extends EnhanceMessageHandler<OrderCreatedEvent> {

    @Override
    public boolean filter(Message message) {
        // Filter by Tag
        return "order-created".equals(message.getTag());
    }

    @Override
    public void handleMessage(OrderCreatedEvent event) {
        orderService.onOrderCreated(event);
    }
}
```

### 4.5 Batch Consume

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

## 5. Advanced Usage / Extension Points

### 5.1 Environment Isolation Principle

When `enabled-isolation: true` is set, the module automatically prefixes Topics with the environment:

| Environment | Original Topic | Actual Topic              |
|-------------|----------------|---------------------------|
| dev         | `ORDER_TOPIC`  | `DEV_ORDER_TOPIC`         |
| test        | `ORDER_TOPIC`  | `TEST_ORDER_TOPIC`        |
| prod        | `ORDER_TOPIC`  | `ORDER_TOPIC` (unchanged) |

This prevents dev/test messages from polluting production.

### 5.2 Message Body Format

```java
RocketMqMessage message = RocketMqMessage.builder()
    .topic("TOPIC")           // Topic
    .tag("TAG")               // Tag for consumer filtering
    .payLoad(Object)          // Message content (any object, auto JSON-serialized)
    .key("KEY")               // Business key for query and deduplication
    .delayTime(Long)          // Delay time (milliseconds)
    .build();
```

### 5.3 Ordered Messages

To send ordered messages, use `MessageQueueSelector` to ensure messages with the same business key enter the same queue:

```java
rocketProducer.syncSendOrderly(message, orderId);
```

---

## 6. Interplay with Other Modules

| Module                      | How They Cooperate                                                                                                |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------|
| `ddf-common-ons`            | If migrating from ONS to self-hosted RocketMQ, consumer abstraction patterns are similar, reducing migration cost |
| `ddf-common-core`           | JSON serialization and utility support                                                                            |
| `ddf-common-authentication` | If message bodies need user context, combine with the auth module's Token mechanism                               |

---

## 7. FAQ

**Q1: What's the relationship between this module and `rocketmq-spring-boot-starter`?**
This module is an enhancement layer on top of the native starter, not a rewrite. All native configurations and annotations remain valid; the module adds unified producer, Java 8 time compatibility, and environment isolation.

**Q2: Does environment isolation affect existing Topics?**
No. `EnvironmentIsolationProcessor` only dynamically modifies the Topic name at send time; consumers subscribe with the same environment prefix, keeping both sides consistent.

**Q3: Are transactional messages supported?**
RocketMQ natively supports transactional messages, but this module does not provide additional encapsulation. For transactional sends, use `RocketMQTemplate`'s transactional API directly.

**Q4: What does the `filter()` method in consumers do?**
`EnhanceMessageHandler`'s `filter()` pre-filters messages before `handleMessage()`. Messages returning `false` are skipped, preventing invalid messages from entering business logic.

---

## 8. References

- Source: `RocketProducer`, `RocketEnhanceProperties`, `EnvironmentIsolationProcessor`, `EnhanceMessageHandler`
- RocketMQ Spring docs: https://github.com/apache/rocketmq-spring
