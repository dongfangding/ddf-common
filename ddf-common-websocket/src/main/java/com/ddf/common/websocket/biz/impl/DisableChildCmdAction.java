package com.ddf.common.websocket.biz.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ddf.common.constant.GlobalConstants;
import com.ddf.common.websocket.biz.ChildCmdAction;
import com.ddf.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.MessageResponse;
import com.ddf.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 禁用子命令码处理类
 *


 */
@Service("DISABLE")
@Slf4j
public class DisableChildCmdAction implements ChildCmdAction {
    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;

    /**
     * 处理子命令码
     *
     * @param baseDevice
     * @param authPrincipal
     * @param message
     * @return


     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message responseCmd(MerchantBaseDevice baseDevice, AuthPrincipal authPrincipal, Message message) {
        LambdaUpdateWrapper<MerchantBaseDevice> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(MerchantBaseDevice::getIsRemoteDisable, GlobalConstants.INTEGER_TRUE);
        updateWrapper.eq(MerchantBaseDevice::getId, baseDevice.getId());
        merchantBaseDeviceService.update(updateWrapper);
        WebsocketSessionStorage.putResponse(message.getRequestId(), MessageResponse.success("禁用成功!"));
        return message;
    }
}
