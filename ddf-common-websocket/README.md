# ddf-common-websocket

> WebSocket support module. Provides connection management, handshake authentication, message sending/receiving, and session storage — suitable for real-time push, instant messaging, and other WebSocket scenarios in Spring Boot applications.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-websocket` solves the **"real-time bidirectional communication in Spring Boot"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Server-side push | Order status changes need to notify clients instantly | `WsMessageServiceImpl.sendCmd` for point-to-point push |
| Instant messaging | Chat messages need to be broadcast to multiple users | `WsMessageServiceImpl.sendCmdAll` for full broadcast |
| Online admin dashboard | Admin needs to see current online connection count | `WebsocketSessionStorage` for unified session management |
| Secure handshake | WebSocket connections need token validation | `HandshakeAuth` extension point |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-websocket</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
ddf:
  websocket:
    endpoint: /ws                      # WebSocket endpoint path
    allowed-origins: "*"               # Allowed origins (specify concrete domains in production)
    max-text-message-buffer-size: 8192 # Text message buffer size
    max-session-idle-timeout: 60000    # Session idle timeout in milliseconds
    message-secret: "your-msg-secret"  # Message encryption key
    rsa-private-key: "..."             # RSA private key (message signing)
    rsa-public-key: "..."              # RSA public key
```

---

## 4. Core API

### 4.1 Client connection

```javascript
const socket = new WebSocket('ws://localhost:8080/ws');

socket.onopen = function() {
    // Send authentication info
    socket.send('AUTH_TOKEN');
};

socket.onmessage = function(event) {
    const msg = JSON.parse(event.data);
    console.log('Received:', msg.cmd, msg.data);
};

socket.send(JSON.stringify({
    cmd: 'ping',
    data: {}
}));
```

### 4.2 Server-side message sending

```java
@Autowired
private WsMessageServiceImpl wsService;

// Single user push
wsService.sendCmd("user123", "ORDER_STATUS_CHANGED", orderData);

// Batch push
wsService.sendCmd(Arrays.asList("user1", "user2"), "NOTIFY", messageData);

// Broadcast to all online users
wsService.sendCmdAll("BROADCAST", broadcastData);

// Blocking send (wait for response)
ResponseData<Map> response = wsService.blockUntilDataFlush(
    "user123",
    MessageRequest.builder()
        .cmd("REQUEST_CMD")
        .data(requestData)
        .build(),
    5, TimeUnit.SECONDS
);
```

### 4.3 Session management

```java
@Autowired
private WebsocketSessionStorage sessionStorage;

// Get current online user count
int count = sessionStorage.size();

// Check if user is online
boolean online = sessionStorage.isOnline("user123");

// Close a specific user's connection
sessionStorage.close("user123");
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Handshake authentication

Implement `HandshakeAuth` to validate user identity during the WebSocket handshake:

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

### 5.2 Custom message handler

Implement `HandlerMessageService` to handle messages of a specific `cmd` type:

```java
@Component
public class ChatMessageHandler implements HandlerMessageService {
    @Override
    public void handle(WebSocketSession session, MessageRequest message) {
        if ("CHAT".equals(message.getCmd())) {
            // Process chat message
            chatService.process(session, message.getData());
        }
    }
}
```

### 5.3 Connection lifecycle listener

Implement `WebSocketHandlerListener` to listen for open, close, and error events:

```java
@Component
public class ConnectionListener implements WebSocketHandlerListener {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("User online: {}", session.getAttributes().get("userId"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("User offline: {}", session.getAttributes().get("userId"));
    }
}
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-redis` | In cluster environments, use Redis Pub/Sub to broadcast messages across instances |
| `ddf-common-authentication` | Token validation can reuse the auth module's `TokenUtil` |
| `ddf-common-core` | JSON serialization, thread pools, and other fundamentals |

---

## 7. FAQ

**Q1: How are messages broadcast to all nodes in a cluster?**
Current `sendCmdAll` only broadcasts to online users on the current instance. In clustered environments, combine with Redis Pub/Sub or RabbitMQ for cross-instance broadcast.

**Q2: Why shouldn't `allowed-origins` always be `*`?**
In production, restrict to specific domains to prevent Cross-Site WebSocket Hijacking (CSWSH) attacks.

**Q3: Is message encryption mandatory?**
No. `message-secret` and RSA keys are for signing/verifying sensitive messages; ordinary notification messages can skip it.

**Q4: How to keep long-lived connections?**
It is recommended that the client implements a heartbeat mechanism (e.g. send `ping` every 30 seconds). The server controls timeout recycling via `max-session-idle-timeout`.

---

## 8. References

- Source: `WebSocketConfig`, `WsMessageServiceImpl`, `WebsocketSessionStorage`
- Spring WebSocket docs: https://docs.spring.io/spring-framework/reference/web/websocket.html
