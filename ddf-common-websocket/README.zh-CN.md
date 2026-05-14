# ddf-common-websocket

> WebSocket 支撑模块。提供连接管理、握手认证、消息收发和 Session 存储能力，
> 适合需要在 Spring Boot 中集成实时推送、即时通讯等 WebSocket 能力的业务场景。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-websocket` 解决的是 **"Spring Boot 应用中实时双向通信"** 问题。

| 场景     | 典型问题                  | 模块提供的能力                                |
|--------|-----------------------|----------------------------------------|
| 服务端推送  | 订单状态变更后需即时通知客户端       | `WsMessageServiceImpl.sendCmd` 点对点推送   |
| 即时通讯   | 聊天消息需要广播给多个用户         | `WsMessageServiceImpl.sendCmdAll` 全量广播 |
| 在线管理后台 | 管理员需要查看当前在线连接数        | `WebsocketSessionStorage` 统一管理 Session |
| 安全握手   | WebSocket 连接需校验 Token | `HandshakeAuth` 扩展点                    |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-websocket</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  websocket:
    endpoint: /ws                      # WebSocket 端点路径
    allowed-origins: "*"               # 允许的来源（生产环境建议指定具体域名）
    max-text-message-buffer-size: 8192 # 文本消息缓冲区大小
    max-session-idle-timeout: 60000    # 会话空闲超时（毫秒）
    message-secret: "your-msg-secret"  # 消息加密密钥
    rsa-private-key: "..."             # RSA 私钥（消息签名）
    rsa-public-key: "..."              # RSA 公钥
```

---

## 4. 核心 API

### 4.1 客户端连接

```javascript
const socket = new WebSocket('ws://localhost:8080/ws');

socket.onopen = function() {
    // 发送认证信息
    socket.send('AUTH_TOKEN');
};

socket.onmessage = function(event) {
    const msg = JSON.parse(event.data);
    console.log('收到消息:', msg.cmd, msg.data);
};

socket.send(JSON.stringify({
    cmd: 'ping',
    data: {}
}));
```

### 4.2 服务端发送消息

```java
@Autowired
private WsMessageServiceImpl wsService;

// 单用户推送
wsService.sendCmd("user123", "ORDER_STATUS_CHANGED", orderData);

// 批量推送
wsService.sendCmd(Arrays.asList("user1", "user2"), "NOTIFY", messageData);

// 广播所有在线用户
wsService.sendCmdAll("BROADCAST", broadcastData);

// 阻塞发送（等待响应）
ResponseData<Map> response = wsService.blockUntilDataFlush(
    "user123",
    MessageRequest.builder()
        .cmd("REQUEST_CMD")
        .data(requestData)
        .build(),
    5, TimeUnit.SECONDS
);
```

### 4.3 Session 管理

```java
@Autowired
private WebsocketSessionStorage sessionStorage;

// 获取当前在线用户数
int count = sessionStorage.size();

// 判断用户是否在线
boolean online = sessionStorage.isOnline("user123");

// 关闭指定用户的连接
sessionStorage.close("user123");
```

---

## 5. 进阶用法 / 扩展点

### 5.1 握手认证

实现 `HandshakeAuth` 接口，在 WebSocket 握手阶段校验用户身份：

```java
@Component
public class TokenHandshakeAuth implements HandshakeAuth {
    @Override
    public boolean authenticate(ServerHttpRequest request, ServerHttpResponse response) {
        String token = extractToken(request);
        return tokenService.validate(token);
    }
}
```

### 5.2 自定义消息处理器

实现 `HandlerMessageService` 处理特定 `cmd` 类型的消息：

```java
@Component
public class ChatMessageHandler implements HandlerMessageService {
    @Override
    public void handle(WebSocketSession session, MessageRequest message) {
        if ("CHAT".equals(message.getCmd())) {
            // 处理聊天消息
            chatService.process(session, message.getData());
        }
    }
}
```

### 5.3 连接生命周期监听

实现 `WebSocketHandlerListener` 监听连接打开、关闭、异常事件：

```java
@Component
public class ConnectionListener implements WebSocketHandlerListener {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("用户上线: {}", session.getAttributes().get("userId"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("用户离线: {}", session.getAttributes().get("userId"));
    }
}
```

---

## 6. 与其他模块协作

| 模块                          | 协作方式                           |
|-----------------------------|--------------------------------|
| `ddf-common-redis`          | 集群环境下可通过 Redis Topic 实现跨实例消息广播 |
| `ddf-common-authentication` | Token 校验可复用认证模块的 TokenUtil     |
| `ddf-common-core`           | JSON 序列化、线程池等基础支撑              |

---

## 7. FAQ

**Q1：集群部署时消息如何广播到所有节点？**
当前 `sendCmdAll` 仅广播到当前实例的在线用户。集群环境建议结合 Redis Pub/Sub 或 RabbitMQ 等消息队列实现跨实例广播。

**Q2：`allowed-origins` 为什么不能一直用 `*`？**
生产环境应限制为具体域名，防止跨站 WebSocket 劫持（CSWSH）攻击。

**Q3：消息加密是必需的吗？**
不是。`message-secret` 和 RSA 密钥用于敏感消息的加签/验签，普通通知类消息可不启用。

**Q4：如何保持长连接？**
建议客户端实现心跳机制（如每 30 秒发送一次 `ping`），服务端通过 `max-session-idle-timeout` 控制超时回收。

---

## 8. 参考

- 源码：`WebSocketConfig`、`WsMessageServiceImpl`、`WebsocketSessionStorage`
- Spring WebSocket 文档：https://docs.spring.io/spring-framework/reference/web/websocket.html
