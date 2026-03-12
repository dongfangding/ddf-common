package com.ddf.boot.common.websocket.handler;

import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.WebSocketSessionWrapper;
import org.springframework.web.socket.TextMessage;

/**
 * @author dongfang.ding
 * @since 2019/8/22 18:30
 */
public interface HandlerMessageService {

    /**
     * 处理接收到的消息
     *
     * @param authPrincipal 认证主体对象
     * @param session 会话对象
     * @param textMessage 文本消息对象
     */
    void handlerMessage(AuthPrincipal authPrincipal, WebSocketSessionWrapper session, TextMessage textMessage);

}
