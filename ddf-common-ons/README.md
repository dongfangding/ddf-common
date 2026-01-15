# ddf-common-ons

阿里云 ONS 消息队列模块。

## 功能特性

- 消息发送
- 消息订阅
- 顺序消息

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-ons</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `OnsProducer` | ONS 生产者 |
| `OnsConsumer` | ONS 消费者 |
| `OnsProperties` | 配置属性 |

## 使用说明

```yaml
ddf:
  ons:
    producer:
      access-key: xxx
      secret-key: xxx
    consumer:
      access-key: xxx
      secret-key: xxx
```
