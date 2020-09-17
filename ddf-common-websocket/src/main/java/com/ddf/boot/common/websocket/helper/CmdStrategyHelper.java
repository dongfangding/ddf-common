package com.ddf.boot.common.websocket.helper;

import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.mq.helper.RabbitTemplateHelper;
import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.Message;
import com.ddf.boot.common.websocket.model.MessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

/**
 * 将一些与指令业务相关的逻辑统一定义在该类中，方便复用
 *
 * @author dongfang.ding
 * @date 2019/9/4 15:41
 */
@Component
@Slf4j
public class CmdStrategyHelper {
    @Autowired
    private ThreadPoolTaskExecutor deviceCmdRunningStatePersistencePool;
    @Autowired
    private RabbitTemplateHelper rabbitTemplateHelper;

    /**
     * 发送设备指令码运行状态数据
     * @param authPrincipal
     * @param message
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

    /**
     * 记录日志并发送消息
     *
     * @param authPrincipal
     * @param payload
     * @param message
     * @param <T>
     */
    public <T> void recordAndSend(AuthPrincipal authPrincipal, T payload, Message<T> message) {
        TextMessage textMessage = Message.wrapper(message);
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setBusinessData(JsonUtil.asString(payload));
//        channelTransferService.recordRequest(authPrincipal, textMessage.getPayload(), message, messageRequest);
        WebsocketSessionStorage.sendMessage(authPrincipal, message);
    }
}



