package com.ddf.boot.common.websocket.biz;

import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import org.springframework.web.socket.TextMessage;

/**
 * @author dongfang.ding
 * @date 2019/8/22 18:30
 */
public interface HandlerMessageService {

    /**
     * 处理接收到的消息
     * @param authPrincipal
     * @param session
     * @param textMessage
     */
    void handlerMessage(AuthPrincipal authPrincipal, WebSocketSessionWrapper session, TextMessage textMessage);

}
