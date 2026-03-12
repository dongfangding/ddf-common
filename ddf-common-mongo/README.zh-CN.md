# ddf-common-mongo

[English](./README.md) | [中文](./README.zh-CN.md)

MongoDB 接入辅助模块。

## 当前定位

- 提供 MongoDB 相关组件扫描入口
- 提供 `MongoTemplateHelper` 等辅助能力
- 当前模块较轻量，更偏 MongoDB 接入辅助而不是完整数据访问框架

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mongo</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.boot.mongo.config.MongoAutoConfiguration`

## 主要类型

- `MongoAutoConfiguration`
- `MongoTemplateHelper`

## 说明

- 当前模块主要建立 Mongo 相关公共能力的组织入口
- 更完整的 repository、查询模型和分页能力建议在业务侧或后续专门模块中沉淀
