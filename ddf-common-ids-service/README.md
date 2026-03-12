# ddf-common-ids-service

[English](./README.md) | [中文](./README.zh-CN.md)

Distributed ID generation service module.

## Current Positioning

- Provides the unified `IdsApi`
- Supports both segment mode and snowflake mode for ID generation
- Segment mode depends on a data source
- Snowflake mode depends on Zookeeper coordination

## Auto-configuration

- `com.ddf.common.ids.service.config.IdsServiceAutoConfiguration`

## Main Types

- `IdsApi`
- `IdsProperties`
- `SnowflakeService`
- `IDAllocDao`

## Configuration Prefix

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

## Segment Mode

Enable it with:

```yaml
customizer:
  infra:
    ids:
      segmentEnable: true
```

Notes:

- Requires an available data source
- Can integrate with a custom `IDAllocDao` backed by your own segment table

## Snowflake Mode

Enable it with:

```yaml
customizer:
  infra:
    ids:
      snowflakeEnable: true
```

Notes:

- Requires an available Zookeeper address
- `IdsApi` is the unified external entry and should be preferred in business code over direct internal implementation dependencies

## Notes

- The implementation follows the Leaf idea and is integrated into the current repository
- Public usage should prefer `IdsApi` rather than directly depending on internal implementation classes
