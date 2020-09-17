package com.ddf.boot.common.websocket.handler;


import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.listeners.WebSocketHandlerListener;
import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.Message;
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
 *
 * 如果想通用， 让使用方还可以除了默认实现以外，还可以在事件上执行一些自己的逻辑，就又要暴露接口。。
 *
 * 这小小的代码本来就没啥含量，为了通用实在是写的一言难尽，把事情搞复杂了。
 *
 * @author dongfang.ding
 * @date 2019/8/20 11:43
 */
@Slf4j
public class DefaultWebSocketHandler extends AbstractWebSocketHandler {

    /**
     * 处理文本消息的事件处理器
     */
    private final HandlerMessageService handlerMessageService;

    /**
     * 为当前实现暴露监听事件， 允许额外实现逻辑
     */
    private final WebSocketHandlerListener webSocketHandlerListener;

    public DefaultWebSocketHandler(HandlerMessageService handlerMessageService, WebSocketHandlerListener webSocketHandlerListener) {
        this.handlerMessageService = handlerMessageService;
        this.webSocketHandlerListener = webSocketHandlerListener;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        AuthPrincipal principal = (AuthPrincipal) session.getPrincipal();
        log.info("[{}-{}-{}]建立连接成功.....", principal.getLoginType(), principal.getAccessKeyId(), principal.getAuthCode());
        WebsocketSessionStorage.active((AuthPrincipal) session.getPrincipal(), session);
        WebsocketSessionStorage.sendMessage((AuthPrincipal) session.getPrincipal(), Message.echo("现在开始可以和服务器通讯了"));
        if (webSocketHandlerListener != null) {
            webSocketHandlerListener.afterConnectionEstablished(session);
        }
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
        if (principal == null || StringUtils.isAnyBlank(principal.getAccessKeyId(), principal.getAuthCode())) {
            session.sendMessage(Message.wrapper(Message.responseNotLogin(textMessage)));
            session.close();
            return;
        }
        log.info("[{}-{}-{}]收到消息: {}]", principal.getLoginType(), principal.getAccessKeyId(), principal.getAuthCode(), textMessage.getPayload());
        if (handlerMessageService != null) {
            handlerMessageService.handlerMessage(principal, WebsocketSessionStorage.get(principal), textMessage);
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("-----------------handlePongMessage------------------");
        if (webSocketHandlerListener != null) {
            webSocketHandlerListener.handlePongMessage(session, message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        AuthPrincipal principal = (AuthPrincipal) session.getPrincipal();
        log.info("[{}-{}-{}]handleTransportError.....", principal.getLoginType(), principal.getAccessKeyId(),
                principal.getAuthCode(), exception);
        WebsocketSessionStorage.inactive(principal, session);
        if (webSocketHandlerListener != null) {
            webSocketHandlerListener.handleTransportError(session, exception);
        }
        session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        AuthPrincipal principal = (AuthPrincipal) session.getPrincipal();
        log.info("[{}-{}-{}]afterConnectionClosed.....CloseStatus: {}", principal.getLoginType(),
                principal.getAccessKeyId(), principal.getAuthCode(), status);
        WebsocketSessionStorage.inactive((AuthPrincipal) session.getPrincipal(), session);
        if (webSocketHandlerListener != null) {
            webSocketHandlerListener.afterConnectionClosed(session, status);
        }
    }
}
