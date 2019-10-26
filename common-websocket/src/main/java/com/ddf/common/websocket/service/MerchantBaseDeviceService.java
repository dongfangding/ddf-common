package com.ddf.common.websocket.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;

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
     * @param token token
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

    /**
     * 同步设备的版本
     *
     * @param deviceNumber 设备号
     * @param token 设备绑定时的随机码
     * @return
     * @author dongfang.ding
     * @date 2019/9/24 17:15
     */
    int syncVersionList(String deviceNumber, String token);

    /**
     * 根据设备号获取设备记录
     *
     * @param deviceNumber
     * @return
     * @author dongfang.ding
     * @date 2019/9/29 10:29
     */
    MerchantBaseDevice getByDeviceNumber(String deviceNumber);

}
