package com.ddf.boot.common.websocket.listeners;

import com.ddf.boot.common.websocket.handler.DefaultWebSocketHandler;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * <p>给{@link DefaultWebSocketHandler} 暴露的允许使用方额外根据事件执行逻辑 </p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/09/17 09:52
 */
public interface WebSocketHandlerListener {

    /**
     * 连接建立事件
     *
     * @param session 会话对象
     */
    void afterConnectionEstablished(WebSocketSession session) throws Exception;

    /**
     * 收到二进制消息事件
     *
     * @param session 会话对象
     * @param message 消息内容
     */
    void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception;

    /**
     * 处理pong事件
     *
     * @param session 会话对象
     * @param message 消息内容
     */
    void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception;

    /**
     * 传输异常事件
     *
     * @param session 会话对象
     * @param exception 异常对象
     */
    void handleTransportError(WebSocketSession session, Throwable exception) throws Exception;

    /**
     * 连接关闭事件
     *
     * @param session 会话对象
     * @param status 状态参数
     */
    void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception;
}
