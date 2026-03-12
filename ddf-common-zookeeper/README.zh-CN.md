# ddf-common-zookeeper

[English](./README.md) | [中文](./README.zh-CN.md)

Zookeeper 相关工具与监听支撑模块。

## 当前定位

- 提供 Zookeeper 自动配置入口
- 提供节点监听与监控相关辅助能力
- 可作为分布式 ID、分布式锁等场景的底层支撑模块

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-zookeeper</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.boot.zookeeper.ZookeeperAutoConfiguration`

## 主要类型

- `NodeEventListener`
- `MonitorProperties`
- `MonitorRegistryConfig`

## 说明

- 当前模块更偏工具和监控辅助，不是完整的服务注册发现框架封装
