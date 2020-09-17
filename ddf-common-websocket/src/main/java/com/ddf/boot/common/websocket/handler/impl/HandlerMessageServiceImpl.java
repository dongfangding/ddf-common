package com.ddf.boot.common.websocket.handler.impl;

import com.ddf.boot.common.core.exception200.BadRequestException;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.websocket.enumu.InternalCmdEnum;
import com.ddf.boot.common.websocket.handler.HandlerMessageService;
import com.ddf.boot.common.websocket.helper.CmdAction;
import com.ddf.boot.common.websocket.helper.CmdStrategyHelper;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.Message;
import com.ddf.boot.common.websocket.model.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

/**
 *
 * 接收到消息之后的处理
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Service
@Slf4j
public class HandlerMessageServiceImpl implements HandlerMessageService {

    @Autowired
    @Qualifier(value = "handlerMessagePool")
    private ThreadPoolTaskExecutor handlerMessagePool;
    @Autowired
    private ChannelTransferService channelTransferService;
    @Value("${customs.message_secret}")
    private boolean messageSecret;
    @Autowired
    private CmdStrategyHelper cmdStrategyHelper;

    /**
     * 处理接收到的消息
     *
     * @param webSocketSessionWrapper
     * @param textMessage
     */
    @Override
    public void handlerMessage(AuthPrincipal authPrincipal, WebSocketSessionWrapper webSocketSessionWrapper, TextMessage textMessage) {
        doMessageConsumer(authPrincipal, webSocketSessionWrapper, textMessage);
    }


    /**
     * 安卓认证用户收到数据之后的业务处理
     * @param authPrincipal
     * @param webSocketSessionWrapper
     * @param textMessage
     */
    private void doMessageConsumer(AuthPrincipal authPrincipal, WebSocketSessionWrapper webSocketSessionWrapper, TextMessage textMessage) {
        handlerMessagePool.execute(() -> {
            if (InternalCmdEnum.PING.name().equals(textMessage.getPayload())) {
                try {
                    WebsocketSessionStorage.get(authPrincipal).getWebSocketSession().sendMessage(new TextMessage(InternalCmdEnum.PONG.name()));
                } catch (IOException e) {
                    log.error("响应心跳包出错！", e);
                }
                return;
            }
            Message<?> message = null;
            String messageStr = textMessage.getPayload();
            try {
                if (messageSecret) {
                    message = Message.unSign(textMessage.getPayload());
                    if (message == null) {
                        throw new BadRequestException("验签不通过!");
                    }
                } else {
                    message = Message.toMessage(textMessage);
                }
                messageStr = JsonUtil.asString(message);
            } catch (Exception e) {
                log.error("客户端发送数据格式有误或验签不通过！ 数据内容：{} ", textMessage.getPayload(), e);
                // 不接受客户端其他格式的数据
                WebsocketSessionStorage.sendMessageAndClose(webSocketSessionWrapper, Message.echo("数据格式有误！关闭连接！"));
                channelTransferService.recordResponse(authPrincipal, null, textMessage.getPayload(), null);
            }
            try {
                if (message == null) {
                    return;
                }
                // 记录指令码响应状态
                if (Message.Type.RESPONSE.equals(message.getType())) {
                    cmdStrategyHelper.buildDeviceCmdRunningState(authPrincipal, message, true);
                }
                int code = channelTransferService.recordResponse(authPrincipal, message.getRequestId(),
                        messageStr, message);
                if (code == -1) {
                    WebsocketSessionStorage.sendMessage(webSocketSessionWrapper, Message.responseNotMatchRequest(message));
                    log.warn("请求不匹配：{}", message);
                    return;
                } else if (code == 1) {
                    WebsocketSessionStorage.sendMessage(webSocketSessionWrapper, Message.responseRepeatRequest(message));
                    log.warn("重复请求: {}", message);
                    return;
                }
                new CmdAction(message.getCmd()).responseCmd(webSocketSessionWrapper, authPrincipal, message);
            } catch (Exception e) {
                log.error("处理客户端数据出错！", e);
            }
        });
    }
}
