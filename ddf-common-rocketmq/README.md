# ddf-common-rocketmq

[English](./README.md) | [中文](./README.zh-CN.md)

RocketMQ enhancement integration module.

## Current Positioning

- Provides an enhancement layer based on `rocketmq-spring-boot-starter`
- Provides the enhanced `RocketProducer`
- Handles Jackson message conversion compatibility for Java time types
- Supports property-driven environment isolation behavior

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-rocketmq</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.boot.common.rocketmq.config.RocketMQEnhanceAutoConfiguration`

## Main Types

- `RocketProducer`
- `RocketEnhanceProperties`
- `EnvironmentIsolationProcessor`

## Related Configuration

Environment isolation switch:

```yaml
rocketmq:
  enhance:
    enabledIsolation: true
```

## Notes

- This module is not a rewrite of the full RocketMQ capability set, but an enhancement layer on top of Spring RocketMQ integration
- Standard RocketMQ connection parameters should still be configured through the native starter conventions
