# ddf-common-mongo

[English](./README.md) | [中文](./README.zh-CN.md)

MongoDB integration helper module.

## Current Positioning

- Provides the component scanning entry for MongoDB-related support
- Provides helper capabilities such as `MongoTemplateHelper`
- The current module is lightweight and is closer to MongoDB integration assistance than to a full data access framework

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mongo</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.boot.mongo.config.MongoAutoConfiguration`

## Main Types

- `MongoAutoConfiguration`
- `MongoTemplateHelper`

## Notes

- The module mainly establishes a shared organizational entry for Mongo-related support
- Fuller repository, query-model, and paging capabilities should be built in business code or later dedicated modules
