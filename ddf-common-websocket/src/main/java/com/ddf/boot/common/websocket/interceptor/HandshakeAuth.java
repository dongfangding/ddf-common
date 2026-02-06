package com.ddf.boot.common.websocket.interceptor;

import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.HandshakeParam;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

/**
 *
 * @author dongfang.ding
 * @since 2019/12/27 0027 13:44
 */
public interface HandshakeAuth {

    /**
     * 基于默认的握手实现，校验认证参数，返回认证身份，可以提供多个实现
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @param handshakeParam
     * @return
     * @see DefaultHandshakeInterceptor
     */
    AuthPrincipal validPrincipal(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes, HandshakeParam handshakeParam);
}
