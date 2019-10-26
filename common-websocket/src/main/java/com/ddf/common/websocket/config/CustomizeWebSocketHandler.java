package com.ddf.common.websocket.config;


import com.ddf.common.util.SpringContextHolder;
import com.ddf.common.websocket.biz.HandlerMessageService;
import com.ddf.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
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
 * @author dongfang.ding
 * @date 2019/8/20 11:43
 */
@Slf4j
public class CustomizeWebSocketHandler extends AbstractWebSocketHandler {


    private HandlerMessageService handlerMessageService = SpringContextHolder.getBean(HandlerMessageService.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[{}]建立连接成功.....", session.getPrincipal());
        WebsocketSessionStorage.active(session.getPrincipal(), session);
        session.sendMessage(Message.wrapper(Message.echo("现在开始可以和服务器通讯了")));
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
        if (principal == null || StringUtils.isAnyBlank(principal.getIme(), principal.getToken())) {
            session.sendMessage(Message.wrapper(Message.responseNotLogin(textMessage)));
            session.close();
            return;
        }
        log.info("[{}-{}]收到消息: {}]", principal.getIme(), principal.getToken(), textMessage.getPayload());
        handlerMessageService.handlerMessage(principal, WebsocketSessionStorage.get(principal), textMessage);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("-----------------handlePongMessage------------------");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("[{}]handleTransportError.....", session.getPrincipal());
        WebsocketSessionStorage.inactive(session.getPrincipal(), session);
        session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[{}]afterConnectionClosed.....", session.getPrincipal());
        WebsocketSessionStorage.inactive(session.getPrincipal(), session);
    }


}
