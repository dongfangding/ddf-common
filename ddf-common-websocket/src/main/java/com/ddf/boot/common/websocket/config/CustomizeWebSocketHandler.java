package com.ddf.boot.common.websocket.config;


import com.ddf.boot.common.util.SpringContextHolder;
import com.ddf.boot.common.websocket.biz.HandlerMessageService;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 *
 * 自定义实现消息处理器以及事件的监听
 *

 * @date 2019/8/20 11:43
 */
@Slf4j
public class CustomizeWebSocketHandler extends AbstractWebSocketHandler {


    private HandlerMessageService handlerMessageService = SpringContextHolder.getBean(HandlerMessageService.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[{}]建立连接成功.....", session.getPrincipal());
        WebsocketSessionStorage.active((AuthPrincipal) session.getPrincipal(), session);
        WebsocketSessionStorage.sendMessage((AuthPrincipal) session.getPrincipal(), Message.echo("现在开始可以和服务器通讯了"));
    }

    /**
     * 待测试，这个方法好像是同步的
     * @param session
     * @param textMessage
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        log.info("-----------------handleTextMessage------------------");
        AuthPrincipal principal = (AuthPrincipal) session.getPrincipal();
        if (principal == null || StringUtils.isAnyBlank(principal.getDeviceNumber(), principal.getToken())) {
            session.sendMessage(Message.wrapper(Message.responseNotLogin(textMessage)));
            session.close();
            return;
        }
        log.info("[{}-{}]收到消息: {}]", principal.getDeviceNumber(), principal.getToken(), textMessage.getPayload());
        handlerMessageService.handlerMessage(principal, WebsocketSessionStorage.get(principal), textMessage);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("-----------------handlePongMessage------------------");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("[{}]handleTransportError.....", session.getPrincipal(), exception);
        WebsocketSessionStorage.inactive((AuthPrincipal) session.getPrincipal(), session);
        session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[{}]afterConnectionClosed.....CloseStatus: {}", session.getPrincipal(), status);
        WebsocketSessionStorage.inactive((AuthPrincipal) session.getPrincipal(), session);
    }


}
