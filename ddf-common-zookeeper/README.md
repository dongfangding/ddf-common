# ddf-common-zookeeper

[English](./README.md) | [中文](./README.zh-CN.md)

Zookeeper utility and listener support module.

## Current Positioning

- Provides the Zookeeper auto-configuration entry
- Provides helper capabilities for node listeners and monitoring
- Can serve as a lower-level support module for distributed IDs, distributed locks, and similar scenarios

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-zookeeper</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Auto-configuration

- `com.ddf.boot.zookeeper.ZookeeperAutoConfiguration`

## Main Types

- `NodeEventListener`
- `MonitorProperties`
- `MonitorRegistryConfig`

## Notes

- The module is currently more utility and monitoring oriented rather than a full service registry or discovery framework
