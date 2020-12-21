package com.ddf.boot.common.websocket.handler;

import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.WebSocketSessionWrapper;
import org.springframework.web.socket.TextMessage;

/**
 * @author dongfang.ding
 * @date 2019/8/22 18:30
 */
public interface HandlerMessageService {

    /**
     * 处理接收到的消息
     *
     * @param authPrincipal
     * @param session
     * @param textMessage
     */
    void handlerMessage(AuthPrincipal authPrincipal, WebSocketSessionWrapper session, TextMessage textMessage);

}
