# CLAUDE.md

## 模块简介

提供基于 Netty 的自定义协议实现，包含 TCP 服务端/客户端、编解码器和 SSL 支持。

## 核心类

| 类路径                                                     | 功能     |
|---------------------------------------------------------|--------|
| `com.ddf.boot.netty.broker.server.BrokerServer`         | 服务端    |
| `com.ddf.boot.netty.broker.client.TCPClient`            | 客户端    |
| `com.ddf.boot.netty.broker.codec.RequestContentCodec`   | 编解码器   |
| `com.ddf.boot.netty.broker.ssl.KeyManagerFactoryHelper` | SSL 工具 |

## 使用说明

### 1. 启动服务端

```java
@Autowired
private BrokerServer brokerServer;

@PostConstruct
public void start() {
    brokerServer.start();
}

@PreDestroy
public void stop() {
    brokerServer.close();
}
```

### 2. 配置

```yaml
ddf:
  netty-broker:
    port: 8888                       # 服务端口
    so-rec-buf: 65535                # 接收缓冲区
    so-snd-buf: 65535                // 发送缓冲区
    ssl:
      enabled: false                 # 是否启用 SSL
      server-jks-path:               # SSL 证书路径
      server-jks-password:           # SSL 密码
```

### 3. 自定义消息

```java
// 定义消息请求
RequestContent<Map<String, String>> request = RequestContent.request(
    RequestContent.Cmd.ECHO.name(),  // 命令类型
    data                              // 消息数据
);
request.addExtra("key", "value");    // 添加扩展字段

// 发送消息
client.write(request);
```

### 4. 消息类型

```java
// 支持的命令类型
RequestContent.Cmd.ECHO      // 回显
RequestContent.Cmd.BIZ       // 业务消息
// 自定义命令...
```

## 协议格式

```
+------------------+------------------+------------------+
|   Magic (4B)     |  Length (4B)     |   Cmd (4B)       |
+------------------+------------------+------------------+
|    Data (JSON)   |              Extra (JSON)          |
+------------------+-------------------------------------+
```

## SSL 配置

```java
// 生成 SSL 证书
// keytool -genkey -alias server_jks -keysize 2048 -validity 365 \
// -keyalg RSA -dname "CN=localhost" -keypass your_password \
// -storepass your_password -keystore server.jks
```

## 注意事项

1. **资源释放**：使用 `brokerServer.close()` 正确释放资源
2. **连接管理**：客户端实现了重连机制，但有最大重试限制
3. **SSL 密码**：生产环境禁止使用默认密码
4. **线程模型**：Boss 处理连接，Worker 处理读写
