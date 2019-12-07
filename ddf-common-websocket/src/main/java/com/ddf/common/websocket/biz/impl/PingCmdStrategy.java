package com.ddf.common.websocket.biz.impl;

import com.ddf.common.websocket.biz.CmdStrategy;
import com.ddf.common.websocket.constant.WebsocketConst;
import com.ddf.common.websocket.dubbo.MessageWsDubboService;
import com.ddf.common.websocket.enumerate.CmdEnum;
import com.ddf.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.common.websocket.model.entity.ChannelTransfer;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.common.websocket.service.ChannelTransferService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Date;

/**
 * 响应客户端发送的心跳包指令.
 *
 * 由于该指令与其它指令的类型不再一致，因此这个类不能再实现接口{@link CmdStrategy}
 *


 */
@Service("PING")
@Slf4j
public class PingCmdStrategy implements CmdStrategy {

    @Autowired
    @Qualifier("channelTransferPool")
    private ThreadPoolTaskExecutor channelTransferPool;
    @Autowired
    private ChannelTransferService channelTransferService;
    @Reference(version = WebsocketConst.DUBBO_VERSION, group = "${spring.profiles.active:local}")
    private MessageWsDubboService messageWsDubboService;

    /**
     * 由于该指令与其它指令的类型不再一致。因此该方法逻辑被剥离，不要在调用了，
     * 请使用最新的方法{@link PingCmdStrategy#responseCmd}
     *
     * @param webSocketSessionWrapper
     * @param authPrincipal
     * @param message
     * @see PingCmdStrategy#responseCmd
     *
     */
    @Override
    @Deprecated
    public Message responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal, Message message) {
        return null;
    }

    /**
     * 响应心跳包
     *
     * @param authPrincipal
     * @param textMessage
     */
    @Override
    public void responseCmd(AuthPrincipal authPrincipal, TextMessage textMessage) {
        doSomethingAtPing(authPrincipal);
        try {
            WebsocketSessionStorage.get(authPrincipal).getWebSocketSession().sendMessage(new TextMessage(CmdEnum.PONG.name()));
        } catch (IOException e) {
            log.error("响应心跳包出错！", e);
        }
    }

    /**
     * 在客户端心跳发送的时候对其进行一些数据的检查或者什么事情之类的
     *
     * @param authPrincipal
     * @return
     */
    private void doSomethingAtPing(AuthPrincipal authPrincipal) {
        channelTransferPool.execute(() -> {
            ChannelTransfer preLog = channelTransferService.getPreLog(authPrincipal.getIme(), CmdEnum.UPGRADE.name());
            if (preLog != null) {
                Date createDate = preLog.getCreateTime();
                // 短期内不要多次发送
                if (DateUtils.addMinutes(createDate, 5).before(new Date())) {
                    return;
                }
            }
        });
    }
}
