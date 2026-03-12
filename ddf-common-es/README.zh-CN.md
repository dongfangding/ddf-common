# ddf-common-es

[English](./README.md) | [中文](./README.zh-CN.md)

Elasticsearch 依赖接入模块。

## 当前定位

- 提供 `spring-boot-starter-data-elasticsearch` 依赖接入
- 不额外封装独立客户端 API
- 适合作为上层 Elasticsearch 业务模块的基础依赖

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-es</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 说明

- 当前模块主要是依赖层封装，不是完整的 Elasticsearch 业务 SDK
- 如果后续沉淀索引管理、查询封装等能力，建议在此模块之上继续扩展
