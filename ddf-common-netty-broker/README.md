# ddf-common-netty-broker

Netty 自定义协议模块，提供基于 Netty 的协议实现。

## 功能特性

- 自定义协议编解码
- 报文加密传输
- 连接管理

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-netty-broker</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `NettyServer` | Netty 服务端 |
| `ProtocolEncoder` | 协议编码器 |
| `ProtocolDecoder` | 协议解码器 |

## 使用说明

配置 Netty 服务端参数，启动服务监听。
