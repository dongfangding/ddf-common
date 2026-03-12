# ddf-common-websocket

[English](./README.md) | [中文](./README.zh-CN.md)

WebSocket support module that provides connection registration, handshake extensions, and message-handling foundations.

## Current Positioning

- Provides WebSocket configuration registration
- Provides handshake authentication and interceptor extension points
- Provides default message handling and listener support
- The current implementation is infrastructure-oriented rather than an annotation-driven chat framework

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-websocket</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Core Configuration Types

- `com.ddf.boot.common.websocket.config.WebSocketConfig`
- `com.ddf.boot.common.websocket.properties.WebSocketProperties`

## Main Extension Points

- `HandshakeAuth`
- `HandlerMessageService`
- `WebSocketHandlerListener`

## Notes

- The current module still contains some business-coupled traces, so adoption should be evaluated against your own business model first
- If it is to remain a long-term public component, more business-specific parts should continue to be separated later
