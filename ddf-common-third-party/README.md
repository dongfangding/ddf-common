# ddf-common-third-party

[English](./README.md) | [中文](./README.zh-CN.md)

Third-party service integration module.

## Current Positioning

- Provides a unified entry point for third-party service integrations
- Currently focuses on Alibaba Cloud OSS and SMS-related capabilities
- Offers reusable integration support for higher-level business modules

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-third-party</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.boot.common.ext.ExtAutoConfiguration`
- `com.ddf.boot.common.ext.oss.config.OssBeanAutoConfiguration`

## Main Types

- `OssProperties`
- `OssHelper`
- `AliYunSmsProperties`

## Notes

- The currently exposed public capabilities mainly focus on OSS and SMS
- If more third-party integrations are added later, they should continue to be split by service domain and documented separately
