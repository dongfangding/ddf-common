package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.SpringContextHolder;
import com.ddf.boot.common.websocket.biz.ChildCmdAction;
import com.ddf.boot.common.websocket.biz.CmdStrategy;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.payload.ChildCmdPayload;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 简单指令响应处理
 *
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Service("SIMPLE")
@Slf4j
public class SimpleCmdStrategy implements CmdStrategy {
    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;
    @Autowired
    private ChannelTransferService channelTransferService;

    /**
     * 响应Cmd命令码
     *
     * @param webSocketSessionWrapper
     * @param authPrincipal
     * @param message
     */
    @Override
    public Message responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal, Message message) {
        String businessData = channelTransferService.getPayloadByRequestId(message.getRequestId());
        ChildCmdPayload childCmdPayload = JsonUtil.toBean(businessData, ChildCmdPayload.class);
        MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
        if (baseDevice == null) {
            log.warn("未找到设备！！【{}】", authPrincipal);
            return message;
        }

        ChildCmdAction childCmdAction = null;
        try {
            childCmdAction = (ChildCmdAction) SpringContextHolder.getBean(childCmdPayload.getChildCmd().name());
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("子命令码【{}】没有定义处理类！", childCmdPayload.getChildCmd().name());
        }
        if (childCmdAction != null) {
            return childCmdAction.responseCmd(baseDevice, authPrincipal, message);
        }

        return message;
    }
}
