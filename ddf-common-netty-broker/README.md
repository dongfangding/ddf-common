# ddf-common-netty-broker

> Netty-based custom protocol communication module. Provides TCP server/client, protocol encoding/decoding,
> SSL encrypted transport, and connection management for scenarios requiring self-built long-connection gateways or private protocols.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-netty-broker` solves the **"application needs a high-performance self-built TCP long-connection channel"** problem.

| Scenario                         | Typical Problem                                                          | What the Module Provides                                                           |
|----------------------------------|--------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Private-protocol device access   | Devices use non-standard protocols; HTTP is insufficient                 | Custom codec that parses binary streams by protocol format                         |
| High-concurrency message gateway | Need to maintain massive long connections; sync blocking can't handle it | Netty NIO event-driven model supports tens of thousands of connections per machine |
| Encrypted packet transport       | Public network transport needs anti-eavesdropping and tamper-proofing    | SSL/TLS certificate encrypted channel                                              |
| Server-initiated push            | Need to actively push commands to clients                                | Bidirectional full-duplex TCP; server can write anytime                            |

> **Note**: This module is **not** in the default Maven Central release set (see `excludeArtifacts` in parent `pom.xml`). For use, build locally or configure separately.

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-netty-broker</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

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

## 4. Core API

### 4.1 Start Server

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

### 4.2 Build Custom Message

```java
RequestContent<Map<String, String>> request = RequestContent.request(
    RequestContent.Cmd.ECHO.name(),
    data
);
request.addExtra("traceId", UUID.randomUUID().toString());

// Send message
client.write(request);
```

### 4.3 Protocol Format

The module's built-in binary protocol format:

```
+------------------+------------------+------------------+
|   Magic (4B)     |  Length (4B)     |   Cmd (4B)       |
+------------------+------------------+------------------+
|    Data (JSON)   |              Extra (JSON)          |
+------------------+-------------------------------------+
```

- **Magic**: Fixed magic number for rapid valid-connection identification
- **Length**: Total packet length
- **Cmd**: Command type (e.g., ECHO, BIZ)
- **Data**: Business data, JSON-serialized
- **Extra**: Extension fields for traceId, version, and other metadata

### 4.4 Supported Command Types

```java
RequestContent.Cmd.ECHO      // Echo test
RequestContent.Cmd.BIZ       // Business message
// Custom commands can be extended in business layer
```

---

## 5. Advanced Usage / Extension Points

### 5.1 SSL Encrypted Connection

Generate certificate and enable SSL:

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

### 5.2 Custom Codec

Implement the `RequestContentCodec` interface to replace default encoding/decoding:

```java
@Component
public class CustomCodec extends RequestContentCodec {
    @Override
    protected void encode(ChannelHandlerContext ctx, RequestContent msg, ByteBuf out) {
        // Custom serialization logic
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Custom deserialization logic
    }
}
```

### 5.3 TCP Client and Reconnection

```java
@Autowired
private TCPClient tcpClient;

// Client implements auto-reconnect; max retry count is configurable
tcpClient.connect("localhost", 8888);
tcpClient.write(request);
```

---

## 6. Interplay with Other Modules

| Module                 | How They Cooperate                                                      |
|------------------------|-------------------------------------------------------------------------|
| `ddf-common-core`      | Utility and JSON serialization support                                  |
| `ddf-common-websocket` | Can be combined when both WebSocket and TCP long connections are needed |

---

## 7. FAQ

**Q1: What's the difference from WebSocket?**
WebSocket is HTTP-handshake-based, suitable for browsers. This module uses raw TCP, ideal for devices, mobile apps, or scenarios needing extreme performance.

**Q2: How are SSL certificates rotated?**
Certificates are loaded dynamically via `KeyManagerFactoryHelper`; restart the server to take effect. In production, pair with certificate management tools for auto-rotation.

**Q3: What's the connection limit?**
Depends on server configuration (file descriptors, memory). Netty's event-driven model theoretically supports hundreds of thousands of connections per machine, limited by OS parameters in practice.

**Q4: How does the server push to clients proactively?**
Maintain all active connections via `ChannelGroup`, then iterate and write back to achieve broadcast or point-to-point push.

---

## 8. References

- Source: `BrokerServer`, `TCPClient`, `RequestContentCodec`, `KeyManagerFactoryHelper`
- Netty docs: https://netty.io/wiki/
