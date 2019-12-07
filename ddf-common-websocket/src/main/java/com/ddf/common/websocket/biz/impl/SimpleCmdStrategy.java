package com.ddf.common.websocket.biz.impl;

import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.util.JsonUtil;
import com.ddf.common.util.SpringContextHolder;
import com.ddf.common.websocket.biz.ChildCmdAction;
import com.ddf.common.websocket.biz.CmdStrategy;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.ChildCmdPayload;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 简单指令响应处理
 *


 */
@Service("SIMPLE")
@Slf4j
public class SimpleCmdStrategy implements CmdStrategy {
    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;

    /**
     * 响应Cmd命令码
     *
     * @param webSocketSessionWrapper
     * @param authPrincipal
     * @param message
     */
    @Override
    public Message responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal, Message message) {
        ChildCmdPayload childCmdPayload;
        try {
            childCmdPayload = JsonUtil.toBean(JsonUtil.asString(message.getBody()), ChildCmdPayload.class);
        } catch (Exception e) {
            throw new GlobalCustomizeException("客户端响应数据没有包含子指令码，无法处理！！");
        }

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
