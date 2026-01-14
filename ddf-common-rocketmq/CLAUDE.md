# CLAUDE.md

## 模块简介

提供 RocketMQ 生产者和消费者增强功能。

## 核心类

| 类路径                                                                    | 功能      |
|------------------------------------------------------------------------|---------|
| `com.ddf.boot.common.rocketmq.producer.RocketProducer`                 | 消息生产者   |
| `com.ddf.boot.common.rocketmq.handler.EnhanceMessageHandler`           | 消息处理器基类 |
| `com.ddf.boot.common.rocketmq.domain.RocketMqMessage`                  | 消息实体    |
| `com.ddf.boot.common.rocketmq.config.RocketMQEnhanceAutoConfiguration` | 自动配置    |

## 使用说明

### 1. 发送消息

```java
@Autowired
private RocketProducer rocketProducer;

// 同步发送
RocketMqMessage message = RocketMqMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-created")
    .payLoad(orderData)              // 消息内容
    .build();

rocketProducer.syncSend(message);

// 发送延迟消息
RocketMqMessage delayMessage = RocketMqMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-timeout")
    .payLoad(orderData)
    .delayTime(3000)                 // 延迟 3 秒
    .build();

rocketProducer.syncSend(delayMessage);

// 异步发送
rocketProducer.asyncSend(RocketMqMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-created")
    .payLoad(orderData)
    .build(), new SendCallback() {
        @Override
        public void onSuccess(SendResult sendResult) {
            // 发送成功
        }

        @Override
        public void onException(Throwable e) {
            // 发送失败
        }
    });
```

### 2. 消费消息

```java
@Component
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",
    consumerGroup = "order-consumer-group"
)
public class OrderConsumer extends EnhanceMessageHandler<OrderCreatedEvent> {

    @Override
    public boolean filter(Message message) {
        // 消息过滤逻辑
        return "order-created".equals(message.getTag());
    }

    @Override
    public void handleMessage(OrderCreatedEvent event) {
        // 处理消息
        orderService.onOrderCreated(event);
    }
}
```

### 3. 批量消费

```java
@Component
@RocketMQMessageListener(
    topic = "BATCH_TOPIC",
    consumerGroup = "batch-consumer-group"
)
public class BatchConsumer extends EnhanceBatchMessageHandler<OrderEvent> {

    @Override
    public void handleMessage(List<OrderEvent> events) {
        // 批量处理
        orderService.batchProcess(events);
    }
}
```

## 配置说明

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
```

## 消息格式

```java
RocketMqMessage message = RocketMqMessage.builder()
    .topic("TOPIC")           // 主题
    .tag("TAG")               // 标签
    .payLoad(Object)          // 消息内容（对象）
    .key("KEY")               // 消息 Key
    .delayTime(Long)          // 延迟时间（毫秒）
    .build();
```

## 注意事项

1. **顺序消息**：需要使用 `MessageQueueSelector` 确保消息发送到同一队列
2. **事务消息**：RocketMQ 原生支持事务消息，本模块未封装
3. **消费者继承**：`EnhanceMessageHandler` 已实现消息过滤、重试等逻辑
4. **线程池**：消费者可配置独立线程池处理消息
