package com.ddf.common.websocket.biz.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.util.JsonUtil;
import com.ddf.common.websocket.biz.CmdStrategy;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.RunningState;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 客户端向服务端报备设备应用运行状态
 *


 */
@Service("RUNNING_STATE")
@Slf4j
public class RunningStateCmdStrategy implements CmdStrategy {
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
    @Transactional(rollbackFor = Exception.class)
    public Message responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal, Message message) {
        String stateStr = JsonUtil.asString(message.getBody());
        RunningState runningState = JsonUtil.toBean(stateStr, RunningState.class);
        MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
        if (baseDevice == null) {
            throw new GlobalCustomizeException("设备信息不存在！");
        }
        LambdaUpdateWrapper<MerchantBaseDevice> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(MerchantBaseDevice::getId, baseDevice.getId());
        // 由于客户端实现方式，这个字段并不是完整的状态json串，因此意义不大
        updateWrapper.set(MerchantBaseDevice::getAppRunningState, stateStr);
        RunningState.UnionPay unionPay = runningState.getUnionPay();
        if (unionPay == null) {
            throw new GlobalCustomizeException("云闪付应用状态相关数据为空!");
        }
        boolean isUpdate = false;
        // 为空或与数据库中一致时不更新状态
        if (unionPay.getLoginState() != null && !Objects.equals(baseDevice.getFlushLoginStatus(), unionPay.getLoginState())) {
            updateWrapper.set(MerchantBaseDevice::getFlushLoginStatus, unionPay.getLoginState());
            isUpdate = true;
        }
        // 为空或与数据库中一致时不更新状态
        if (unionPay.getRunningState() != null && !Objects.equals(baseDevice.getFlushStatus(), unionPay.getRunningState())) {
            updateWrapper.set(MerchantBaseDevice::getFlushStatus, unionPay.getRunningState());
            isUpdate = true;
        }
        if (isUpdate) {
            merchantBaseDeviceService.update(updateWrapper);
        }
        return message;
    }
}
