# ddf-common-ons

[English](./README.md) | [中文](./README.zh-CN.md)

Alibaba Cloud ONS integration module.

## Current Positioning

- Provides producer-related ONS auto-configuration
- Provides support for listener containers and console client components
- Suitable for RocketMQ-compatible integration scenarios based on Alibaba Cloud ONS

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-ons</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.common.ons.config.OnsAutoConfiguration`

## Main Types

- `OnsProperties`
- `OnsClientConfiguration`
- `OnsListenerContainerConfiguration`

## Configuration Prefix

```yaml
customizer:
  infra:
    ons:
      accessKey: your-access-key
      secretKey: your-secret-key
      nameServerAddr: your-ons-endpoint
```

## Notes

- The module currently focuses on ONS producer and listener support
- Topic, tag, and consumer business logic should still be implemented in the business layer
