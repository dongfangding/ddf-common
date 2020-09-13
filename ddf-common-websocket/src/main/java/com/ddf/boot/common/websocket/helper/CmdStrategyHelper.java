package com.ddf.boot.common.websocket.helper;

import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import com.ddf.boot.common.mq.exception.MqSendException;
import com.ddf.boot.common.mq.helper.RabbitTemplateHelper;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.exception.MessageFormatInvalid;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDeviceRunningState;
import com.ddf.boot.common.websocket.model.ws.*;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import com.ddf.boot.common.websocket.service.PlatformMessageTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private PlatformMessageTemplateService platformMessageTemplateService;
    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;
    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;
    @Autowired
    private ChannelTransferService channelTransferService;
    @Autowired
    @Qualifier("handlerMatchTemplateExecutor")
    private ThreadPoolTaskExecutor handlerMatchTemplateExecutor;
    @Autowired
    @Qualifier("handlerMessageBusiness")
    private ThreadPoolTaskExecutor handlerMessageBusiness;
    @Autowired
    private ThreadPoolTaskExecutor deviceCmdRunningStatePersistencePool;
    @Autowired
    private RabbitTemplateHelper rabbitTemplateHelper;

    /**
     * 针对批次的报文，对单个报文响应
     *
     * @param key
     * @param keyValue
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    public static Map<String, Object> buildErrorMap(String key, String keyValue, String errorMessage, boolean retry) {
        Map<String, Object> errorMap = new HashMap<>(3);
        errorMap.put(key, keyValue);
        errorMap.put("code", MessageResponse.SERVER_CODE_ERROR);
        errorMap.put("message", errorMessage);
        errorMap.put("retry", retry);
        return errorMap;
    }

    public static Map<String, Object> buildSmsErrorMap(String keyValue, String errorMessage) {
        return buildErrorMap("primaryKey", keyValue, errorMessage, false);
    }

    public static Map<String, Object> buildUPayBillOrderErrorMap(String keyValue, String errorMessage) {
        return buildErrorMap("tradeNo", keyValue, errorMessage, false);
    }

    /**
     * 针对批次的报文，对单个报文响应
     *
     * @param key
     * @param keyValue
     * @return
     * @author dongfang.ding
     */
    public static Map<String, Object> buildSuccessMap(String key, String keyValue) {
        Map<String, Object> errorMap = new HashMap<>(4);
        errorMap.put(key, keyValue);
        errorMap.put("code", MessageResponse.SERVER_CODE_COMPLETE);
        errorMap.put("message", "处理成功");
        errorMap.put("retry", false);
        return errorMap;
    }

    public static Map<String, Object> buildSmsSuccessMap(String keyValue) {
        return buildSuccessMap("primaryKey", keyValue);
    }

    public static Map<String, Object> buildUPayBillOrderSuccessMap(String keyValue, String billType, Integer qrCodeType) {
        Map<String, Object> map = buildSuccessMap("tradeNo", keyValue);
        map.put("billType", billType);
        map.put("qrCodeType", qrCodeType);
        return map;
    }

    /**
     * 获取报文主体数据 对象
     *
     * @param message
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapPayload(Message message, String errorMessage, boolean isThrowable) {
        if (message == null || message.getBody() == null || "".equals(message.getBody())) {
            throw new MessageFormatInvalid(errorMessage);
        }
        Map<String, Object> payload;
        try {
            payload = (Map) message.getBody();
        } catch (Exception e) {
            log.error(errorMessage + "报文格式: [{}]", message.getBody(), e);
            throw new MessageFormatInvalid(errorMessage);
        }
        if (payload == null || payload.isEmpty()) {
            if (isThrowable) {
                throw new MessageFormatInvalid(errorMessage);
            }
        }
        return payload;
    }

    /**
     * 获取报文主体数据 对象
     *
     * @param message
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapPayload(Message message, String errorMessage) {
        return getMapPayload(message, errorMessage, true);
    }

    /**
     * 获取报文主体数据 对象数组
     *
     * @param message
     * @param errorMessage
     * @param isThrowable 报文数据为空时是否抛出异常
     * @return
     * @author dongfang.ding
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getListPayload(Message message, String errorMessage, boolean isThrowable) {
        if (message == null || message.getBody() == null || "".equals(message.getBody())) {
            throw new MessageFormatInvalid(errorMessage);
        }
        List<Map<String, Object>> payload;
        try {
            payload = (List) message.getBody();
        } catch (Exception e) {
            log.error(errorMessage + "报文格式: [{}]", message.getBody(), e);
            throw new MessageFormatInvalid(errorMessage);
        }
        if (payload == null || payload.isEmpty()) {
            if (isThrowable) {
                throw new MessageFormatInvalid(errorMessage);
            }
        }
        return payload;
    }

    public static List<Map<String, Object>> getListPayload(Message message, String errorMessage) {
        return getListPayload(message, errorMessage, true);
    }

    /**
     * 发送设备指令码运行状态数据
     * @param authPrincipal
     * @param message
     * @param isResponse 是否时响应数据
     */
    public void buildDeviceCmdRunningState(AuthPrincipal authPrincipal, Message<?> message, boolean isResponse) {
        deviceCmdRunningStatePersistencePool.execute(() -> {
            if (message == null || authPrincipal == null || CmdEnum.PONG.equals(message.getCmd())
                    || CmdEnum.PING.equals(message.getCmd())) {
                return;
            }
            MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
            MerchantBaseDeviceRunningState runningState = new MerchantBaseDeviceRunningState();
            runningState.setDeviceId(baseDevice.getId()).setCmd(message.getCmd().name()).setRequestId(message.getRequestId())
                    .setRequestTime(message.getTimestamp()).setStatus(DeviceRunningStateStatus.RUNNING.getStatus())
                    .setResponseFlag(isResponse).setId(IdsUtil.getNextLongId());

            if (isResponse) {
                runningState.setResponseTime(message.getTimestamp()).setStatus(DeviceRunningStateStatus.OVER.getStatus());
            }

            try {
                rabbitTemplateHelper.wrapperAndSend(QueueBuilder.QueueDefinition.DEVICE_CMD_RUNNING_STATE_PERSISTENCE_QUEUE, runningState);
            } catch (MqSendException e) {
                log.error("发送设备状态数据监控报错！数据为： {}", JsonUtil.asString(runningState), e);
            }
        });
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
        channelTransferService.recordRequest(authPrincipal, textMessage.getPayload(), message, messageRequest);
        WebsocketSessionStorage.sendMessage(authPrincipal, message);
    }
}



