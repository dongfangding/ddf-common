package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.websocket.biz.CmdStrategy;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.helper.CmdStrategyHelper;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.entity.ChannelTransfer;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 响应客户端发送的心跳包指令.
 *
 * 由于该指令与其它指令的类型不再一致，因此这个类不能再实现接口{@link CmdStrategy}
 *

 * @date 2019/8/27 13:43
 */
@Service("PING")
@Slf4j
public class PingCmdStrategy implements CmdStrategy {

    @Autowired
    @Qualifier("channelTransferPool")
    private ThreadPoolTaskExecutor channelTransferPool;
    @Autowired
    private ChannelTransferService channelTransferService;
    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;
    @Autowired
    private CmdStrategyHelper cmdStrategyHelper;


    private static LoadingCache<String, Boolean> dataSyncCache = CacheBuilder.newBuilder()
            .initialCapacity(1).maximumSize(1)
            .expireAfterWrite(WebsocketConst.DATA_SYNC_INTERVAL, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Boolean>() {
                @Override
                public Boolean load(String key) throws Exception {
                    return false;
                }
            });

    private static LoadingCache<String, Boolean> appVersionCache = CacheBuilder.newBuilder()
            .initialCapacity(1).maximumSize(1)
            .expireAfterWrite(WebsocketConst.APP_VERSION_SYNC, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Boolean>() {
                @Override
                public Boolean load(String key) throws Exception {
                    return false;
                }
            });

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
    public void responseCmd(AuthPrincipal authPrincipal, TextMessage textMessage){
        doSomethingAtPing(authPrincipal);
        try {
            WebsocketSessionStorage.get(authPrincipal).getWebSocketSession().sendMessage(new TextMessage(CmdEnum.PONG.name()));
        } catch (IOException e) {
            log.error("响应心跳包出错！", e);
        } catch (IllegalStateException exception) {
            log.error("websocket连接出现了不可逆转的错误，关闭连接，等待客户端重新连接。。。", exception);
        }
    }

    /**
     * 在客户端心跳发送的时候对其进行一些数据的检查或者什么事情之类的
     *
     * @param authPrincipal
     * @return

     * @date 2019/9/24 16:02
     */
    private void doSomethingAtPing(AuthPrincipal authPrincipal) {
        checkVersionList(authPrincipal);
        checkUnionPayDataSync(authPrincipal);
    }

    /**
     * 检查设备版本列表，如果与期望版本不同，推送更新清单
     * @param authPrincipal
     */
    @SuppressWarnings("unchecked")
    private void checkVersionList(AuthPrincipal authPrincipal) {
        channelTransferPool.execute(() -> {
            if (appVersionCache.getUnchecked(CmdEnum.UPGRADE.name())) {
                return;
            }
        });
    }

    /**
     * 校验该设备是否已同步过相关个人资料
     * @param authPrincipal
     */
    private void checkUnionPayDataSync(AuthPrincipal authPrincipal) {
        channelTransferPool.execute(() -> {
            if (dataSyncCache.getUnchecked(CmdEnum.DATA_SYNC.name())) {
                return;
            }
            // 短期内不重复发送的判断
            List<ChannelTransfer> preLogs = channelTransferService.getTodayLog(authPrincipal.getAccessKeyId(), CmdEnum.DATA_SYNC.name(), false);
            if (preLogs != null && !preLogs.isEmpty()) {
                Date lastDate = preLogs.get(0).getCreateTime();
                // 短期内不要多次发送
                if (DateUtils.addMinutes(lastDate, WebsocketConst.DATA_SYNC_INTERVAL).after(new Date())) {
                    // 在服务器未宕机的前提下，至少避免多次查库来判断是否已同步
                    dataSyncCache.put(CmdEnum.DATA_SYNC.name(), true);
                    return;
                }
            }
        });
    }
}
