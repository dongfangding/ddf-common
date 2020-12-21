package com.ddf.boot.common.websocket.service;

import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.Message;
import com.ddf.boot.common.websocket.model.WebSocketSessionWrapper;
import org.springframework.web.socket.TextMessage;

/**
 * 命令码的策略接口
 * 每个实现的Service的beanName必须和命令码保持一致
 *
 * @author dongfang.ding
 * @date 2019/8/24 10:35
 */
public interface CmdStrategy {


    /**
     * 将指令下发给设备
     *
     * @param cmd
     * @param clientChannel
     * @param body
     * @param <T>
     * @return
     */
    default <T> Message<T> push(String cmd, String clientChannel, T body) {
        return Message.request(cmd, clientChannel, body);
    }

    /**
     * 响应Cmd命令码
     *
     * @param webSocketSessionWrapper
     * @param authPrincipal
     * @param message
     * @return
     * @author dongfang.ding
     * @date 2019/10/26 15:17
     */
    <T> Message<T> responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal,
            Message<T> message);

    /**
     * 响应Cmd命令码
     *
     * @param authPrincipal
     * @param textMessage
     */
    default void responseCmd(AuthPrincipal authPrincipal, TextMessage textMessage) {

    }

}
