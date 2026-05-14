package com.ddf.boot.common.websocket.helper;

import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.Message;
import com.ddf.boot.common.websocket.model.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.TextMessage;

/**
 * 将一些与指令业务相关的逻辑统一定义在该类中，方便复用
 *
 * @author dongfang.ding
 * @since 2019/9/4 15:41
 */
@Slf4j
@RequiredArgsConstructor
public class CmdStrategyHelper {
    /**
     * @param authPrincipal 参数
     * @param payload 参数
     * @param message 参数
     */
    private final ThreadPoolTaskExecutor deviceCmdRunningStatePersistencePool;

    /**
     * 记录日志并发送消息
     *
     * @param authPrincipal 认证主体对象
     * @param payload 事件载荷对象
     * @param message 消息内容
     * @param <T> 泛型类型
     */
    public <T> void recordAndSend(AuthPrincipal authPrincipal, T payload, Message<T> message) {
        TextMessage textMessage = Message.wrapper(message);
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setBusinessData(JsonUtil.asString(payload));
        //        channelTransferService.recordRequest(authPrincipal, textMessage.getPayload(), message, messageRequest);
        WebsocketSessionStorage.sendMessage(authPrincipal, message);
    }

    /**
     * 发送设备指令码运行状态数据
     *
     * @param authPrincipal 认证主体对象
     * @param message 消息内容
     * @param isResponse 是否时响应数据
     */
    public void buildDeviceCmdRunningState(AuthPrincipal authPrincipal, Message<?> message, boolean isResponse) {
        //        deviceCmdRunningStatePersistencePool.execute(() -> {
        //            if (message == null || authPrincipal == null || CmdEnum.PONG.equals(message.getCmd())
        //                    || CmdEnum.PING.equals(message.getCmd())) {
        //                return;
        //            }
        //            MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
        //            MerchantBaseDeviceRunningState runningState = new MerchantBaseDeviceRunningState();
        //            runningState.setDeviceId(baseDevice.getId()).setCmd(message.getCmd().name()).setRequestId(message.getRequestId())
        //                    .setRequestTime(message.getTimestamp()).setStatus(DeviceRunningStateStatus.RUNNING.getStatus())
        //                    .setResponseFlag(isResponse).setId(IdsUtil.getNextLongId());
        //
        //            if (isResponse) {
        //                runningState.setResponseTime(message.getTimestamp()).setStatus(DeviceRunningStateStatus.OVER.getStatus());
        //            }
        //
        //            try {
        //                rabbitTemplateHelper.wrapperAndSend(QueueBuilder.QueueDefinition.DEVICE_CMD_RUNNING_STATE_PERSISTENCE_QUEUE, runningState);
        //            } catch (MqSendException e) {
        //                log.error("发送设备状态数据监控报错！数据为： {}", JsonUtil.asString(runningState), e);
        //            }
        //        });
    }
}
