package com.ddf.boot.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.mapper.MerchantBaseDeviceMapper;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        LambdaUpdateWrapper<MerchantBaseDevice> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(MerchantBaseDevice::getNumber, authPrincipal.getDeviceNumber());
        updateWrapper.eq(MerchantBaseDevice::getRandomCode, authPrincipal.getToken());
        updateWrapper.eq(MerchantBaseDevice::getRemoved, 0);
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
            WebsocketSessionStorage.modifySync(AuthPrincipal.buildAndroidAuthPrincipal(authPrincipal.getToken(), authPrincipal.getDeviceNumber()), true);
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

    /**
     * 查询设备是否有效
     *
     * @param ime   设备号
     * @param token randomCode
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isValid(String ime, String token) {
        LambdaQueryWrapper<MerchantBaseDevice> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantBaseDevice::getNumber, ime);
        queryWrapper.eq(MerchantBaseDevice::getRandomCode, token);
        queryWrapper.eq(MerchantBaseDevice::getBindingType, 1);
        queryWrapper.eq(MerchantBaseDevice::getRemoved, 0);
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
        updateWrapper.eq(MerchantBaseDevice::getRemoved, 0);

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
        if (authPrincipal == null || StringUtils.isAnyBlank(authPrincipal.getDeviceNumber(), authPrincipal.getToken())) {
            return null;
        }
        LambdaQueryWrapper<MerchantBaseDevice> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantBaseDevice::getRemoved, 0);
        queryWrapper.eq(MerchantBaseDevice::getNumber, authPrincipal.getDeviceNumber());
        queryWrapper.eq(MerchantBaseDevice::getRandomCode, authPrincipal.getToken());
        return getOne(queryWrapper);
    }
}
