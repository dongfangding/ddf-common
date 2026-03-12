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
 * @since 2019/8/24 10:35
 */
public interface CmdStrategy {


    /**
     * 将指令下发给设备
     *
     * @param cmd 命令参数
     * @param clientChannel 客户端通道参数
     * @param body 请求体参数
     * @param <T> 泛型类型
     * @return
     */
    default <T> Message<T> push(String cmd, String clientChannel, T body) {
        return Message.request(cmd, clientChannel, body);
    }

    /**
     * 响应Cmd命令码
     *
     * @param webSocketSessionWrapper WebSocket 会话包装对象
     * @param authPrincipal 认证主体对象
     * @param message 消息内容
     * @return
     * @author dongfang.ding
     * @since 2019/10/26 15:17
     */
    <T> Message<T> responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal,
            Message<T> message);

    /**
     * 响应Cmd命令码
     *
     * @param authPrincipal 认证主体对象
     * @param textMessage 文本消息对象
     */
    default void responseCmd(AuthPrincipal authPrincipal, TextMessage textMessage) {

    }

}
