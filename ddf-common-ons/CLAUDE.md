# CLAUDE.md

## 模块简介

提供阿里云消息队列 ONS（MQ for Apache RocketMQ）集成支持。

## 核心类

| 类路径                                                                 | 功能      |
|---------------------------------------------------------------------|---------|
| `com.ddf.common.ons.producer.OnsProducer`                           | 生产者     |
| `com.ddf.common.ons.consumer.AbstractOrdinaryOnsMessageListener`    | 普通消息消费者 |
| `com.ddf.common.ons.consumer.order.AbstractOrderOnsMessageListener` | 顺序消息消费者 |
| `com.ddf.common.ons.consumer.batch.AbstractBatchOnsMessageListener` | 批量消息消费者 |

## 使用说明

### 1. 配置

```yaml
ddf:
  ons:
    access-key: your-access-key
    secret-key: your-secret-key
    producer:
      group-id: "GID_PRODUCER_GROUP"
    consumer:
      group-id: "GID_CONSUMER_GROUP"
```

### 2. 发送消息

```java
@Autowired
private OnsProducer onsProducer;

// 普通消息
OnsMessage message = OnsMessage.builder()
    .topic("TOPIC_NAME")
    .tag("TAG_NAME")
    .payLoad(orderData)
    .build();
onsProducer.send(message);

// 顺序消息
OnsMessage orderMessage = OnsMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order")
    .payLoad(orderData)
    .shardingKey(orderId)  // 顺序消息的分片键
    .build();
onsProducer.orderSend(orderMessage);

// 异步发送
onsProducer.sendAsync(OnsMessage.builder()
    .topic("TOPIC_NAME")
    .tag("TAG")
    .payLoad(data)
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

### 3. 消费消息

#### 普通消息消费者

```java
@Component
@OnsMessageListener(
    consumerGroup = "GID_CONSUMER_GROUP",
    topic = "TOPIC_NAME",
    tag = "*"
)
public class OrderConsumer extends AbstractOrdinaryOnsMessageListener<OrderEvent> {

    @Override
    public void executeBiz(OrderEvent event) {
        // 业务处理
        orderService.process(event);
    }

    @Override
    public boolean isBizSuccess(OrderEvent event) {
        // 判断业务是否成功
        return event != null && event.isValid();
    }
}
```

#### 顺序消息消费者

```java
@Component
@OnsMessageListener(
    consumerGroup = "GID_ORDER_GROUP",
    topic = "ORDER_TOPIC",
    tag = "order"
)
public class OrderSequenceConsumer extends AbstractOrderOnsMessageListener<OrderEvent> {

    @Override
    public void executeBiz(OrderEvent event) {
        // 按顺序处理消息
        orderService.processInOrder(event);
    }
}
```

#### 批量消息消费者

```java
@Component
@OnsMessageListener(
    consumerGroup = "GID_BATCH_GROUP",
    topic = "BATCH_TOPIC",
    tag = "*"
)
public class BatchConsumer extends AbstractBatchOnsMessageListener<OrderEvent> {

    @Override
    public void executeBiz(List<OrderEvent> events) {
        // 批量处理
        orderService.batchProcess(events);
    }
}
```

## 消息类型对比

| 类型      | 说明       | 适用场景   |
|---------|----------|--------|
| 普通消息    | 无序、并行消费  | 大多数场景  |
| 顺序消息    | 按分片键顺序消费 | 订单、物流等 |
| 定时/延迟消息 | 指定时间投递   | 延时任务   |
| 事务消息    | 半消息 + 回查 | 分布式事务  |

## 注意事项

1. **顺序消息**：消费失败返回 `Suspend` 会挂起队列，建议做好幂等处理
2. **批量消费**：一条消息失败会导致整批重试，建议业务层拆分处理
3. **身份认证**：需要配置阿里云 AccessKey 和 SecretKey
4. **网络隔离**：阿里云 ONS 有公网和 VPC 内网两种接入方式
