# ddf-common-ons

> Alibaba Cloud ONS (MQ for Apache RocketMQ) integration module. Provides producer auto-configuration and
> ordinary/ordered/batch consumer abstractions for RocketMQ-compatible messaging on Alibaba Cloud.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-ons` solves the **"business systems need to integrate with Alibaba Cloud ONS message queue"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Cloud-native message push on Alibaba Cloud | Self-hosted RocketMQ cluster ops cost is high | Use Alibaba Cloud managed ONS, zero ops |
| Async order status processing | Order creation needs async notification to multiple downstreams | Ordinary messages + Tag filtering, parallel consumption |
| Sequential order flow processing | Changes to the same order must be consumed in order | Ordered messages, `shardingKey` guarantees sequence |
| Bulk data synchronization | Logs and tracking data volume is large; single-message consumption is inefficient | Batch message consumer processes multiple messages at once |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ons</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
customizer:
  infra:
    ons:
      access-key: your-access-key
      secret-key: your-secret-key
      name-server-addr: http://your-ons-endpoint.cn-hangzhou.mq-internal.aliyuncs.com:8080
```

---

## 4. Core API

### 4.1 Send Ordinary Message

```java
@Autowired
private OnsProducer onsProducer;

OnsMessage message = OnsMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-created")
    .payLoad(orderData)
    .build();

onsProducer.send(message);
```

### 4.2 Send Ordered Message

```java
OnsMessage orderMessage = OnsMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-status-changed")
    .payLoad(orderData)
    .shardingKey(orderId)  // Messages with same orderId are consumed in order
    .build();

onsProducer.orderSend(orderMessage);
```

### 4.3 Async Send

```java
onsProducer.sendAsync(OnsMessage.builder()
    .topic("NOTIFY_TOPIC")
    .tag("sms")
    .payLoad(smsData)
    .build(), new SendCallback() {
        @Override
        public void onSuccess(SendResult sendResult) {
            log.info("ONS send success: {}", sendResult.getMessageId());
        }

        @Override
        public void onException(Throwable e) {
            log.error("ONS send failed", e);
        }
    });
```

### 4.4 Consume Ordinary Message

```java
@Component
@OnsMessageListener(
    consumerGroup = "GID_ORDER_GROUP",
    topic = "ORDER_TOPIC",
    tag = "*"
)
public class OrderConsumer extends AbstractOrdinaryOnsMessageListener<OrderEvent> {

    @Override
    public void executeBiz(OrderEvent event) {
        orderService.process(event);
    }

    @Override
    public boolean isBizSuccess(OrderEvent event) {
        return event != null && event.isValid();
    }
}
```

### 4.5 Consume Ordered Message

```java
@Component
@OnsMessageListener(
    consumerGroup = "GID_ORDER_SEQ_GROUP",
    topic = "ORDER_TOPIC",
    tag = "order-status-changed"
)
public class OrderSequenceConsumer extends AbstractOrderOnsMessageListener<OrderEvent> {

    @Override
    public void executeBiz(OrderEvent event) {
        // Messages under same shardingKey are executed sequentially
        orderService.processInOrder(event);
    }
}
```

### 4.6 Batch Consume Messages

```java
@Component
@OnsMessageListener(
    consumerGroup = "GID_BATCH_GROUP",
    topic = "LOG_TOPIC",
    tag = "*"
)
public class LogBatchConsumer extends AbstractBatchOnsMessageListener<LogEvent> {

    @Override
    public void executeBiz(List<LogEvent> events) {
        logService.batchSave(events);
    }
}
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Message Type Comparison

| Type | Description | Suitable For |
| --- | --- | --- |
| Ordinary | Unordered, parallel consumption | Most async notification scenarios |
| Ordered | Sequence guaranteed by `shardingKey` | Order status flow, inventory deduction |
| Scheduled/Delay | Deliver at specified time | Timeout cancellation, delayed tasks |
| Transactional | Half-message + checkback | Distributed transaction eventual consistency |

### 5.2 Consumer Thread Pool Configuration

```java
@Bean
public ThreadPoolExecutor onsConsumerExecutor() {
    return new ThreadPoolExecutor(
        4, 8, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000),
        new ThreadFactoryBuilder().setNameFormat("ons-consumer-%d").build()
    );
}
```

### 5.3 Environment Isolation

Use different `name-server-addr` for dev/test/prod isolation:

```yaml
---
spring:
  config:
    activate:
      on-profile: dev
customizer:
  infra:
    ons:
      name-server-addr: http://ons-dev.aliyuncs.com:8080
---
spring:
  config:
    activate:
      on-profile: prod
customizer:
  infra:
    ons:
      name-server-addr: http://ons-prod.aliyuncs.com:8080
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-mvc` | Unified response format and global exception handling |
| `ddf-common-core` | JSON serialization, logging, and utility support |
| `ddf-common-rocketmq` | If migrating to self-hosted RocketMQ later, business code switching cost is relatively low |

---

## 7. FAQ

**Q1: What's the difference between ONS and self-hosted RocketMQ?**
ONS is Alibaba Cloud's managed RocketMQ-compatible service with SLA guarantees and zero ops. Self-hosted RocketMQ has lower cost but requires self-operation.

**Q2: What happens if ordered message consumption fails?**
Ordered message consumption failure returning `Suspend` will block the entire shard queue; subsequent messages are stalled. Implement idempotency and exception isolation in business layer.

**Q3: How is a single failed message handled in batch consumption?**
In batch mode, one failed message causes the entire batch to retry. Catch exceptions per message inside `executeBiz` to prevent one failure from affecting the whole batch.

**Q4: How to switch between public internet and VPC internal network?**
Change `name-server-addr` to the corresponding endpoint. VPC internal network has lower latency and no traffic charges; recommended for production.

---

## 8. References

- Source: `OnsProducer`, `AbstractOrdinaryOnsMessageListener`, `AbstractOrderOnsMessageListener`, `AbstractBatchOnsMessageListener`
- Alibaba Cloud ONS docs: https://www.alibabacloud.com/help/en/mq
