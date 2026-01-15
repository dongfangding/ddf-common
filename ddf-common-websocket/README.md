# ddf-common-websocket

WebSocket 模块，提供实时通信功能。

## 功能特性

- WebSocket 连接管理
- 消息推送
- 集群消息转发
- 消息加密

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-websocket</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径                | 功能            |
|--------------------|---------------|
| `WebSocketServer`  | WebSocket 服务端 |
| `WebSocketSender`  | 消息发送器         |
| `WsSessionManager` | 会话管理          |

## 使用说明

```java
@WebSocket("/ws")
public class MyWebSocket extends WebSocketServer {

    @Override
    public void onMessage(String message) {
        // 处理消息
    }
}
```

### 发送消息

```java
@Autowired
private WebSocketSender sender;

public void sendToUser(String userId, String message) {
    sender.sendToUser(userId, message);
}
```
