# ddf-common-es

[English](./README.md) | [中文](./README.zh-CN.md)

Elasticsearch dependency integration module.

## Current Positioning

- Provides `spring-boot-starter-data-elasticsearch` dependency integration
- Does not wrap an additional standalone client API
- Works as a base dependency for higher-level Elasticsearch business modules

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-es</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Notes

- This module is currently a dependency-layer wrapper rather than a full Elasticsearch business SDK
- If index management or query abstractions are added later, they should be built on top of this module
