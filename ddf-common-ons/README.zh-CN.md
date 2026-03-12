# ddf-common-ons

[English](./README.md) | [中文](./README.zh-CN.md)

阿里云 ONS 集成模块。

## 当前定位

- 提供 ONS 生产者相关自动配置
- 提供监听容器与控制台客户端相关支撑
- 适用于基于阿里云 ONS 的 RocketMQ 兼容接入场景

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ons</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.common.ons.config.OnsAutoConfiguration`

## 主要类型

- `OnsProperties`
- `OnsClientConfiguration`
- `OnsListenerContainerConfiguration`

## 配置前缀

```yaml
customizer:
  infra:
    ons:
      accessKey: your-access-key
      secretKey: your-secret-key
      nameServerAddr: your-ons-endpoint
```

## 说明

- 当前模块主要聚焦 ONS 生产与监听支撑
- 具体 Topic、Tag、消费者业务处理逻辑仍建议在业务侧实现
