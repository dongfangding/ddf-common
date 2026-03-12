# ddf-common-ids-service

[English](./README.md) | [中文](./README.zh-CN.md)

分布式 ID 生成服务模块。

## 当前定位

- 提供统一的 `IdsApi`
- 支持号段模式与雪花模式两种 ID 生成方式
- 号段模式依赖数据源
- 雪花模式依赖 Zookeeper 协调

## 自动配置

- `com.ddf.common.ids.service.config.IdsServiceAutoConfiguration`

## 主要类型

- `IdsApi`
- `IdsProperties`
- `SnowflakeService`
- `IDAllocDao`

## 配置前缀

```yaml
customizer:
  infra:
    ids:
      segmentEnable: false
      snowflakeEnable: true
      name: ids_demo
      beginTimestamp: 1609430400000
      zkAddress: 127.0.0.1:2181
      port: 2181
```

## 号段模式

启用方式：

```yaml
customizer:
  infra:
    ids:
      segmentEnable: true
```

说明：

- 需要可用的数据源
- 可通过自定义 `IDAllocDao` 接入自己的号段表实现

## 雪花模式

启用方式：

```yaml
customizer:
  infra:
    ids:
      snowflakeEnable: true
```

说明：

- 需要可用的 Zookeeper 地址
- `IdsApi` 是统一对外入口，业务侧优先通过它获取 ID

## 说明

- 模块实现来源于 Leaf 思路，并做了当前项目内整合
- 对外使用时建议优先围绕 `IdsApi`，而不是直接依赖内部实现类
