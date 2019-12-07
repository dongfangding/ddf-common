package com.ddf.common.websocket.biz;

import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;

/**
 * 子命令码处理任务接口
 *


 */
public interface ChildCmdAction {

    /**
     * 处理子命令码
     *
     * @param baseDevice 设备
     * @param authPrincipal
     * @param message
     * @return
     */
    Message responseCmd(MerchantBaseDevice baseDevice, AuthPrincipal authPrincipal, Message message);
}
