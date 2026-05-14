# ddf-common-netty-broker

> 基于 Netty 的自定义协议通信模块。提供 TCP 服务端/客户端、协议编解码、SSL 加密传输和连接管理能力，
> 适用于需要自建长连接网关或私有协议通信的场景。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-netty-broker` 解决的是 **"应用需要基于 TCP 自建高性能长连接通道"** 问题。

| 场景       | 典型问题                | 模块提供的能力                 |
|----------|---------------------|-------------------------|
| 私有协议设备接入 | 设备使用非标准协议，HTTP 无法满足 | 自定义编解码器，按协议格式解析二进制流     |
| 高并发消息网关  | 需要维持海量长连接，同步阻塞模型扛不住 | Netty NIO 事件驱动，单机支持数万连接 |
| 报文加密传输   | 公网传输需防窃听和篡改         | SSL/TLS 证书加密通道          |
| 服务端消息推送  | 需要主动向客户端下发指令        | 双向全双工 TCP 连接，服务端随时可写    |

> **注意**：本模块默认不在 Maven Central 发布集合中（见父 `pom.xml` 的 `excludeArtifacts`），如需使用请在本地构建或单独配置。

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-netty-broker</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  netty-broker:
    port: 8888
    so-rec-buf: 65535
    so-snd-buf: 65535
    ssl:
      enabled: false
```

---

## 4. 核心 API

### 4.1 启动服务端

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

### 4.2 构建自定义消息

```java
RequestContent<Map<String, String>> request = RequestContent.request(
    RequestContent.Cmd.ECHO.name(),
    data
);
request.addExtra("traceId", UUID.randomUUID().toString());

// 发送消息
client.write(request);
```

### 4.3 协议格式

模块内置二进制协议格式：

```
+------------------+------------------+------------------+
|   Magic (4B)     |  Length (4B)     |   Cmd (4B)       |
+------------------+------------------+------------------+
|    Data (JSON)   |              Extra (JSON)          |
+------------------+-------------------------------------+
```

- **Magic**：固定魔数，用于快速识别合法连接
- **Length**：整个报文长度
- **Cmd**：命令类型（如 ECHO、BIZ）
- **Data**：业务数据，JSON 序列化
- **Extra**：扩展字段，用于传递 traceId、版本号等元信息

### 4.4 支持的命令类型

```java
RequestContent.Cmd.ECHO      // 回显测试
RequestContent.Cmd.BIZ       // 业务消息
// 可在业务层扩展自定义命令
```

---

## 5. 进阶用法 / 扩展点

### 5.1 SSL 加密连接

生成证书并启用 SSL：

```bash
keytool -genkey -alias server_jks -keysize 2048 -validity 365 \
  -keyalg RSA -dname "CN=localhost" -keypass your_password \
  -storepass your_password -keystore server.jks
```

```yaml
ddf:
  netty-broker:
    ssl:
      enabled: true
      server-jks-path: classpath:server.jks
      server-jks-password: your_password
```

### 5.2 自定义编解码器

实现 `RequestContentCodec` 接口，替换默认的编解码逻辑：

```java
@Component
public class CustomCodec extends RequestContentCodec {
    @Override
    protected void encode(ChannelHandlerContext ctx, RequestContent msg, ByteBuf out) {
        // 自定义序列化逻辑
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 自定义反序列化逻辑
    }
}
```

### 5.3 TCP 客户端与重连

```java
@Autowired
private TCPClient tcpClient;

// 客户端已实现断线重连机制，可配置最大重试次数
tcpClient.connect("localhost", 8888);
tcpClient.write(request);
```

---

## 6. 与其他模块协作

| 模块                     | 协作方式                             |
|------------------------|----------------------------------|
| `ddf-common-core`      | 工具类、JSON 序列化支撑                   |
| `ddf-common-websocket` | 如需同时支持 WebSocket 和 TCP 长连接，可组合使用 |

---

## 7. FAQ

**Q1：与 WebSocket 有什么区别？**
WebSocket 基于 HTTP 握手，适合浏览器端；本模块基于原生 TCP，适合设备端、移动端或需要极致性能的场景。

**Q2：SSL 证书如何轮换？**
通过 `KeyManagerFactoryHelper` 动态加载证书，重启服务端即可生效。生产环境建议配合证书管理工具自动轮换。

**Q3：连接数上限是多少？**
取决于服务器配置（文件描述符、内存）。Netty 的事件驱动模型理论上单机可支持数十万连接，实际受限于 OS 参数。

**Q4：服务端如何主动向客户端推送？**
通过 `ChannelGroup` 维护所有活跃连接，遍历写回即可实现广播或单点推送。

---

## 8. 参考

- 源码：`BrokerServer`、`TCPClient`、`RequestContentCodec`、`KeyManagerFactoryHelper`
- Netty 文档：https://netty.io/wiki/
