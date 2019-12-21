package com.ddf.boot.common.websocket.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;

/**
 * @author dongfang.ding
 * @date 2019/8/21 14:57
 */
public interface MerchantBaseDeviceService extends IService<MerchantBaseDevice> {


    /**
     * 同步设备状态
     *
     * @param authPrincipal
     * @param webSocketSessionWrapper
     * @return
     */
    void sync(AuthPrincipal authPrincipal, WebSocketSessionWrapper webSocketSessionWrapper);


    /**
     * 查询设备是否有效
     *
     * @param ime   设备号
     * @param token randomCode
     * @return
     */
    boolean isValid(String ime, String token);


    /**
     * 指定服务器掉线，将连接到这台服务器的设备下线掉
     * @param serverAddress
     * @param isAll 是否所有设备
     */
    boolean offLine(String serverAddress, boolean isAll);


    /**
     * 根据连接身份获取设备
     * @param authPrincipal
     * @return
     */
    MerchantBaseDevice getByAuthPrincipal(AuthPrincipal authPrincipal);


}
