# ddf-common-ons

> 阿里云消息队列 ONS（MQ for Apache RocketMQ）集成模块。提供生产者自动配置、普通/顺序/批量消费者抽象，
> 适用于基于阿里云 ONS 的 RocketMQ 兼容接入场景。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-ons` 解决的是 **"业务系统需要接入阿里云 ONS 消息队列"** 问题。

| 场景         | 典型问题                | 模块提供的能力                 |
|------------|---------------------|-------------------------|
| 阿里云云原生消息推送 | 自建 RocketMQ 集群运维成本高 | 直接使用阿里云托管 ONS，免运维       |
| 订单状态异步处理   | 订单创建后需异步通知多个下游      | 普通消息 + Tag 过滤，并行消费      |
| 订单流水顺序处理   | 同一订单的变更必须按序消费       | 顺序消息，按 `shardingKey` 保序 |
| 批量数据同步     | 日志、埋点数据量大，单条消费效率低   | 批量消息消费者，一次处理多条          |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ons</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
customizer:
  infra:
    ons:
      access-key: your-access-key
      secret-key: your-secret-key
      name-server-addr: http://your-ons-endpoint.cn-hangzhou.mq-internal.aliyuncs.com:8080
```

---

## 4. 核心 API

### 4.1 发送普通消息

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

### 4.2 发送顺序消息

```java
OnsMessage orderMessage = OnsMessage.builder()
    .topic("ORDER_TOPIC")
    .tag("order-status-changed")
    .payLoad(orderData)
    .shardingKey(orderId)  // 同一 orderId 的消息保序消费
    .build();

onsProducer.orderSend(orderMessage);
```

### 4.3 异步发送

```java
onsProducer.sendAsync(OnsMessage.builder()
    .topic("NOTIFY_TOPIC")
    .tag("sms")
    .payLoad(smsData)
    .build(), new SendCallback() {
        @Override
        public void onSuccess(SendResult sendResult) {
            log.info("ONS 发送成功: {}", sendResult.getMessageId());
        }

        @Override
        public void onException(Throwable e) {
            log.error("ONS 发送失败", e);
        }
    });
```

### 4.4 消费普通消息

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

### 4.5 消费顺序消息

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
        // 同一 shardingKey 下的消息按顺序执行
        orderService.processInOrder(event);
    }
}
```

### 4.6 批量消费消息

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

## 5. 进阶用法 / 扩展点

### 5.1 消息类型选型对比

| 类型      | 说明                 | 适用场景        |
|---------|--------------------|-------------|
| 普通消息    | 无序、并行消费            | 大多数异步通知场景   |
| 顺序消息    | 按 `shardingKey` 保序 | 订单状态流转、库存扣减 |
| 定时/延迟消息 | 指定时间投递             | 超时取消、延时任务   |
| 事务消息    | 半消息 + 回查机制         | 分布式事务最终一致性  |

### 5.2 消费者线程池配置

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

### 5.3 环境隔离

通过配置不同 `name-server-addr` 实现开发/测试/生产环境隔离：

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

## 6. 与其他模块协作

| 模块                    | 协作方式                          |
|-----------------------|-------------------------------|
| `ddf-common-mvc`      | 统一响应格式和全局异常处理                 |
| `ddf-common-core`     | JSON 序列化、日志、工具类支撑             |
| `ddf-common-rocketmq` | 如后续需迁移到自建 RocketMQ，业务代码切换成本较低 |

---

## 7. FAQ

**Q1：ONS 与自建 RocketMQ 有什么区别？**
ONS 是阿里云托管的 RocketMQ 兼容服务，免运维、 SLA 保障，适合不想自建集群的场景。自建 RocketMQ 成本更低但需自行运维。

**Q2：顺序消息消费失败会怎样？**
顺序消息消费失败返回 `Suspend` 会挂起整个分片队列，后续消息阻塞。建议业务层做好幂等和异常隔离。

**Q3：批量消费时一条消息失败怎么处理？**
批量消费模式下，一条消息失败会导致整批重试。建议业务层在 `executeBiz` 内逐条捕获异常，避免单条失败影响整批。

**Q4：如何切换公网/VPC 内网接入？**
修改 `name-server-addr` 为对应 endpoint 即可。VPC 内网延迟更低且无流量费用，推荐生产环境使用。

---

## 8. 参考

- 源码：`OnsProducer`、`AbstractOrdinaryOnsMessageListener`、`AbstractOrderOnsMessageListener`、`AbstractBatchOnsMessageListener`
- 阿里云 ONS 文档：https://help.aliyun.com/product/29532.html
