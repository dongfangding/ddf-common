package com.ddf.boot.common.websocket.config;

import com.ddf.boot.common.websocket.constant.WebsocketConst;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 *
 * TODO WebSocketHandlerDecorator 理解一下这个类的用法
 * 握手处理器,用来提取并向容器中添加建立websocket的session认证信息
 *
 * @author dongfang.ding
 * @date 2019/8/20 18:00
 */
public class CustomizeHandshakeHandler extends DefaultHandshakeHandler {
    /**
     * A method that can be used to associate a user with the WebSocket session
     * in the process of being established. The default implementation calls
     * {@link ServerHttpRequest#getPrincipal()}
     * <p>Subclasses can provide custom logic for associating a user with a session,
     * for example for assigning a name to anonymous users (i.e. not fully authenticated).
     *
     * @param request    the handshake request
     * @param wsHandler  the WebSocket handler that will handle messages
     * @param attributes handshake attributes to pass to the WebSocket session
     * @return the user for the WebSocket session, or {@code null} if not available
     */
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return (Principal) attributes.get(WebsocketConst.PRINCIPAL_KEY);
    }
}
