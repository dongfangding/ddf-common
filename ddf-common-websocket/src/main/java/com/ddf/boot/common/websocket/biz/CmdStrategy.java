package com.ddf.boot.common.websocket.biz;

import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import org.springframework.web.socket.TextMessage;

/**
 * 命令码的策略接口
 * 每个实现的Service的beanName必须和命令码保持一致
 *
 *
 * @author dongfang.ding
 * @date 2019/8/24 10:35
 */
public interface CmdStrategy {


    /**
     * 将指令下发给设备
     * @param cmd
     * @param body
     * @param <T>
     * @return
     */
    default <T> Message push(CmdEnum cmd, T body) {
        return Message.request(cmd, body);
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
    Message responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal, Message message);

    /**
     * 响应Cmd命令码
     *
     * @param authPrincipal
     * @param textMessage
     */
    default void responseCmd(AuthPrincipal authPrincipal, TextMessage textMessage) {

    }

}
