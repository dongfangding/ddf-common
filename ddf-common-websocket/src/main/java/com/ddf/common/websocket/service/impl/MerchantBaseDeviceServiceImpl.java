package com.ddf.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.common.websocket.mapper.MerchantBaseDeviceMapper;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


/**
 * @author dongfang.ding
 * @date 2019/8/21 14:58
 */
@Service
@Slf4j
public class MerchantBaseDeviceServiceImpl extends ServiceImpl<MerchantBaseDeviceMapper, MerchantBaseDevice> implements MerchantBaseDeviceService {

    @Autowired
    private MerchantBaseDeviceMapper merchantBaseDeviceMapper;

    /**
     * 同步设备状态
     *
     * @param authPrincipal
     * @param webSocketSessionWrapper
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sync(AuthPrincipal authPrincipal, WebSocketSessionWrapper webSocketSessionWrapper) {
        if (authPrincipal == null || webSocketSessionWrapper == null) {
            return;
        }
        synchronized (WebsocketSessionStorage.WEB_SOCKET_SESSION_MAP) {
            LambdaUpdateWrapper<MerchantBaseDevice> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(MerchantBaseDevice::getNumber, authPrincipal.getIme());
            updateWrapper.eq(MerchantBaseDevice::getRandomCode, authPrincipal.getToken());
            updateWrapper.eq(MerchantBaseDevice::getIsDel, 0);
            updateWrapper.and((sql) -> sql.le(MerchantBaseDevice::getOnlineChangeTime, webSocketSessionWrapper
                    .getStatusChangeTime()).or().isNull(MerchantBaseDevice::getOnlineChangeTime)
                    // FIXME 多台设备的纳秒时间没有一个相对时间，同一台机器上的纳秒时间有先后
                    .or().ne(MerchantBaseDevice::getConnectServerAddress, webSocketSessionWrapper.getServerAddress())
                    .or().isNull(MerchantBaseDevice::getConnectServerAddress));
            updateWrapper.set(MerchantBaseDevice::getIsOnline, webSocketSessionWrapper.getStatus());
            updateWrapper.set(MerchantBaseDevice::getConnectServerAddress, webSocketSessionWrapper.getServerAddress());
            updateWrapper.set(MerchantBaseDevice::getOnlineChangeTime, webSocketSessionWrapper.getStatusChangeTime());
            boolean update = update(updateWrapper);
            // 上线状态即使未更新成功，也说明同步过了，这条数据并不能保证覆盖状态，下次也没必要再覆盖
            if (webSocketSessionWrapper.getStatus().equals(WebSocketSessionWrapper.STATUS_ON_LINE)) {
                WebsocketSessionStorage.modifySync(AuthPrincipal.buildAndroidAuthPrincipal(authPrincipal.getToken(), authPrincipal.getIme()), true);
            }
            // 下线状态必须更新成功才说明是真的下线，才能把数据移除
            if (update && webSocketSessionWrapper.getStatus().equals(WebSocketSessionWrapper.STATUS_OFF_LINE)) {
                WebsocketSessionStorage.remove(authPrincipal);
                try {
                    webSocketSessionWrapper.getWebSocketSession().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 查询设备是否有效
     *
     * @param ime   设备号
     * @param token token
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isValid(String ime, String token) {
        LambdaQueryWrapper<MerchantBaseDevice> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantBaseDevice::getNumber, ime);
        queryWrapper.eq(MerchantBaseDevice::getRandomCode, token);
        queryWrapper.eq(MerchantBaseDevice::getBindingType, 1);
        queryWrapper.eq(MerchantBaseDevice::getIsDel, 0);
        return count(queryWrapper) > 0;
    }

    /**
     * 指定服务器掉线，将连接到这台服务器的设备下线掉
     *
     * @param serverAddress
     * @param isAll         是否所有服务下线
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offLine(String serverAddress, boolean isAll) {
        LambdaUpdateWrapper<MerchantBaseDevice> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(MerchantBaseDevice::getIsOnline, WebSocketSessionWrapper.STATUS_OFF_LINE);
        updateWrapper.set(MerchantBaseDevice::getOnlineChangeTime, System.nanoTime());
        updateWrapper.eq(MerchantBaseDevice::getIsOnline, WebSocketSessionWrapper.STATUS_ON_LINE);
        updateWrapper.eq(MerchantBaseDevice::getIsDel, 0);

        if (!isAll) {
            if (StringUtils.isBlank(serverAddress)) {
                return false;
            }
            updateWrapper.eq(MerchantBaseDevice::getConnectServerAddress, serverAddress);
        }
        return update(updateWrapper);
    }


    /**
     * 根据连接认证身份获取设备信息
     *
     * @param authPrincipal
     * @return
     */
    @Override
    public MerchantBaseDevice getByAuthPrincipal(AuthPrincipal authPrincipal) {
        if (authPrincipal == null || StringUtils.isAnyBlank(authPrincipal.getIme(), authPrincipal.getToken())) {
            return null;
        }
        LambdaQueryWrapper<MerchantBaseDevice> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantBaseDevice::getIsDel, 0);
        queryWrapper.eq(MerchantBaseDevice::getNumber, authPrincipal.getIme());
        queryWrapper.eq(MerchantBaseDevice::getRandomCode, authPrincipal.getToken());
        return getOne(queryWrapper);
    }


    /**
     * 同步设备的版本
     *
     * @param deviceNumber 设备号
     * @param token        设备绑定时的随机码
     * @return
     * @author dongfang.ding
     * @date 2019/9/24 17:15
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncVersionList(String deviceNumber, String token) {
        if (StringUtils.isAnyBlank(deviceNumber, token)) {
            return 0;
        }
        return merchantBaseDeviceMapper.syncVersionList(deviceNumber, token);
    }

    /**
     * 根据设备号获取设备记录
     *
     * @param deviceNumber
     * @return
     * @author dongfang.ding
     * @date 2019/9/29 10:29
     */
    @Override
    public MerchantBaseDevice getByDeviceNumber(String deviceNumber) {
        if (StringUtils.isBlank(deviceNumber)) {
            return null;
        }
        LambdaQueryWrapper<MerchantBaseDevice> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantBaseDevice::getIsDel, 0);
        queryWrapper.eq(MerchantBaseDevice::getNumber, deviceNumber);
        return getOne(queryWrapper);
    }

}
