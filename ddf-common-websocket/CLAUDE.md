# CLAUDE.md

## 模块简介

提供 WebSocket 集成支持，包括连接管理、消息发送、SSL 加密和集群消息转发。

## 核心类

| 类路径                                                               | 功能         |
|-------------------------------------------------------------------|------------|
| `com.ddf.boot.common.websocket.service.impl.WsMessageServiceImpl` | 消息服务       |
| `com.ddf.boot.common.websocket.handler.DefaultWebSocketHandler`   | 默认处理器      |
| `com.ddf.boot.common.websocket.helper.WebsocketSessionStorage`    | Session 存储 |
| `com.ddf.boot.common.websocket.config.WebSocketConfig`            | 配置类        |
| `com.ddf.boot.common.websocket.properties.WebSocketProperties`    | 配置属性       |

## 使用说明

### 1. 配置

```yaml
ddf:
  websocket:
    endpoint: /ws                      # WebSocket 端点
    allowed-origins: "*"               # 允许的来源（生产环境建议指定）
    max-text-message-buffer-size: 8192 # 文本消息缓冲区大小
    max-session-idle-timeout: 60000    # 会话空闲超时（毫秒）
    message-secret: "your-msg-secret"  # 消息加密密钥
    rsa-private-key: "..."             # RSA 私钥（消息签名）
    rsa-public-key: "..."              # RSA 公钥
```

### 2. 连接 WebSocket

```javascript
// 客户端连接
const socket = new WebSocket('ws://localhost:8080/ws');

// 添加认证信息
socket.onopen = function() {
    socket.send('AUTH_TOKEN');
};

// 发送消息
socket.send(JSON.stringify({
    cmd: 'ping',
    data: {}
}));
```

### 3. 发送消息

```java
@Autowired
private WsMessageServiceImpl wsService;

// 发送消息给单个用户
wsService.sendCmd("user123", "MESSAGE", messageData);

// 发送消息给多个用户
wsService.sendCmd(Arrays.asList("user1", "user2"), "MESSAGE", messageData);

// 发送消息给所有用户
wsService.sendCmdAll("BROADCAST", messageData);

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

### 4. 自定义处理器

```java
@Component
public class CustomWebSocketHandler extends DefaultWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 自定义消息处理
        String payload = message.getPayload();
        // ...
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 连接关闭处理
    }
}
```

## 消息格式

```json
{
  "cmd": "CMD_TYPE",     // 命令类型
  "data": { ... }        // 消息数据
}
```

## 注意事项

1. **CORS 安全**：`allowed-origins` 生产环境建议指定具体域名，不要使用 `*`
2. **会话管理**：`WebsocketSessionStorage` 使用 ConcurrentHashMap 存储会话
3. **消息加密**：支持 SSL/TLS 加密传输，配置证书路径
4. **集群部署**：多实例部署时消息不会自动同步，需要使用 Redis Topic 或其他方案
5. **心跳检测**：建议客户端实现心跳机制保活连接
