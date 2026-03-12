# ddf-common-websocket

[English](./README.md) | [中文](./README.zh-CN.md)

WebSocket 支撑模块，提供连接注册、握手扩展和消息处理基础能力。

## 当前定位

- 提供 WebSocket 配置注册能力
- 提供握手认证与拦截扩展点
- 提供默认消息处理与监听支撑
- 当前实现偏基础设施层，不是注解式聊天框架

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-websocket</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心配置类

- `com.ddf.boot.common.websocket.config.WebSocketConfig`
- `com.ddf.boot.common.websocket.properties.WebSocketProperties`

## 主要扩展点

- `HandshakeAuth`
- `HandlerMessageService`
- `WebSocketHandlerListener`

## 说明

- 当前模块包含一定业务耦合痕迹，接入前建议先评估与你的业务模型是否匹配
- 如果以公共组件形式长期开放，建议后续继续抽离与具体业务无关的部分
