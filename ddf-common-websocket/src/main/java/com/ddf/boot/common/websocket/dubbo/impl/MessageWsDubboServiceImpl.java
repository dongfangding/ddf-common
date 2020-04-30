package com.ddf.boot.common.websocket.dubbo.impl;

import com.ddf.boot.common.util.BeanUtil;
import com.ddf.boot.common.util.JsonUtil;
import com.ddf.boot.common.util.WebUtil;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.dubbo.MessageWsDubboService;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.entity.ChannelTransfer;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.payload.QrCreatePayload;
import com.ddf.boot.common.websocket.model.ws.*;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.TextMessage;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**


 */
@Service(version = WebsocketConst.DUBBO_VERSION, group = "${spring.profiles.active:local}")
@Slf4j
public class MessageWsDubboServiceImpl implements MessageWsDubboService {

    @Autowired
    private ChannelTransferService channelTransferService;
    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Environment environment;
    @Autowired
    private ThreadPoolTaskExecutor batchCmdExecutor;


    /**
     * 批量发送指令，该方法目前只支持异步
     *
     * @param requestList
     * @return

     * @date 2019/10/18 9:49
     */
    @Override
    public MessageResponse<?> executeCmd(List<MessageRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return MessageResponse.fastFailure("参数不能为空！");
        }
        for (MessageRequest<?> messageRequest : requestList) {
            batchCmdExecutor.execute(() -> {
                executeCmd(messageRequest);
            });
        }
        return MessageResponse.confirm();
    }



    /**
     * 对设备进行指令下发
     *
     * @param request
     * @return

     * @date 2019/08/21 11:00
     */
    @Override
    public MessageResponse<?> executeCmd(MessageRequest request) {
        try {
            if (request == null || request.getCmd() == null) {
                return MessageResponse.fastFailure("指令不能为空!");
            }
            if (request.getSendMode() == null) {
                return MessageResponse.fastFailure("请传入指令的发送模式， SINGLE, BATCH, ALL!");
            }
            if (request.getClientChannel() == null) {
                // 本意是想让客户端必须明确自己的调用，但现在必须兼容老版本
                request.setClientChannel(ClientChannel.PAY_MAIN);
//                return MessageResponse.failure(String.format("请传入需要发往的应用目标[%s]", Arrays.toString(ClientChannel.values())));
            }

            // 群发指令同步没有意义
            if (!MessageRequest.SendMode.SINGLE.equals(request.getSendMode())) {
                request.setAsync(false);
            }
            boolean isAll = MessageRequest.SendMode.ALL.equals(request.getSendMode());
            String localAddress = WebUtil.getHost() + ":" + environment.getProperty("server.port");
            if (request.isRedirect() && localAddress.equals(request.getRedirectFrom())) {
                log.warn("来自[{}]的转发数据[{}]本机不能处理！本机IP: [{}], ", request.getRedirectFrom(), request, localAddress);
                return null;
            }
            if (isAll) {
                ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> all = WebsocketSessionStorage.getAll();
                Map<AuthPrincipal, String> messageMap = channelTransferService.batchRecordRequest(all, request);
                if (messageMap != null) {
                    messageMap.forEach((k, v) -> {
                        try {
                            // forEach的return其实是普通循环的continue
                            if (!canSend(request, k)) {
                                return;
                            }
                            all.get(k).getWebSocketSession().sendMessage(new TextMessage(v));
                        } catch (IOException ignored) {
                            log.error(ExceptionUtils.getStackTrace(ignored));
                        }
                    });
                }
                if (!request.isRedirect()) {
                    // 转发
                    log.warn("本机[{}]无法处理请求数据[{}]，转发指令", localAddress, request);
                    request.setRedirect(true);
                    request.setRedirectFrom(localAddress);
                    redisTemplate.convertAndSend(WebsocketConst.REDIRECT_CMD_TOPIC, request);
                    return MessageResponse.successWithNoneRequestId();
                }
                return MessageResponse.successWithNoneRequestId();
            } else {
                // 粗略处理批量设备指令
                if (MessageRequest.SendMode.BATCH.equals(request.getSendMode())) {
                    String[] imeArr, tokenArr;
                    try {
                        imeArr = request.getIme().split(",");
                        tokenArr = request.getToken().split(",");
                        if (imeArr.length != tokenArr.length) {
                            return MessageResponse.fastFailure("deviceNumber token 批量格式有误");
                        }
                    } catch (Exception e) {
                        return MessageResponse.fastFailure("批量指令的设备参数不合法！必须使用逗号分隔，且一一对应");
                    }

                    int loop = 0;
                    for (String deviceNumber : imeArr) {
                        MessageRequest messageRequest = makeRequestSingle(request, deviceNumber, tokenArr[loop]);
                        batchCmdExecutor.execute(() -> sendCmd(messageRequest, localAddress));
                        loop++;
                    }
                    return MessageResponse.successWithNoneRequestId();
                } else {
                    return sendCmd(request, localAddress);
                }
            }
        } catch (Exception e) {
            log.error("指令下发失败！指令参数: [{}]", request, e);
            return MessageResponse.fastFailure(e.getMessage());
        }
    }

    /**
     * 尝试从本地缓存获取数据
     * @param request
     * @return
     */
    private MessageResponse<?> tryLoadByLocalCache(MessageRequest request, AuthPrincipal authPrincipal) {
        try {
            if (CmdEnum.QRCODE_CREATE.equals(request.getCmd())) {
                MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
                QrCreatePayload qrCreatePayload = JsonUtil.toBean(JsonUtil.asString(request.getPayload()), QrCreatePayload.class);
            }
        } catch (Exception e) {
            log.error("处理指令[{}]发送前检查失败！", request);
        }
        return null;
    }


    /**
     * 将批量发送的指令构建成单个设备指令参数
     *
     * @param request
     * @param deviceNumber
     * @param token
     * @return
     */
    private MessageRequest<?> makeRequestSingle(MessageRequest<?> request, String deviceNumber, String token) {
        MessageRequest messageRequest = BeanUtil.copy(request, MessageRequest.class);
        return messageRequest
                .setPayload(request.getPayload())
                .setToken(token)
                .setIme(deviceNumber)
                .toSingle();
    }

    /**
     * 校验是否可以发送指令
     * @param request
     * @param authPrincipal
     * @return
     */
    private boolean canSend(MessageRequest request, AuthPrincipal authPrincipal) {
        if (request.isCheckLastTime()) {
            // 如果需要检查上次发送时间，则必须在指定的间隔时间之后
            List<ChannelTransfer> preLogs = channelTransferService.getTodayLog(authPrincipal.getDeviceNumber(), request.getCmd().name(), true);
            if (preLogs != null && !preLogs.isEmpty()) {
                // 小于等于0，代表不限制；如果限制了的话，今日次数如果已大于参数，则不可以发送指令
                if (request.getDailyMaxTimes() > 0 && preLogs.size() > request.getDailyMaxTimes()) {
                    log.warn("今日已发送次数: {}, 限额次数: {}", preLogs.size(), request.getDailyMaxTimes());
                    return false;
                }
                Date lastDate = preLogs.get(0).getCreateTime();
                // 如果没有达到间隔时间，则不发送指令
                if (request.getSendMinutesInterval() > 0 && DateUtils.addMinutes(lastDate, request.getSendMinutesInterval()).after(new Date())) {
                    log.warn("该指令没有达到指定[{}]间隔时间，不允许再次发送！上次发送时间: {}", request.getSendMinutesInterval(),
                            lastDate);
                    return false;
                }
            }
        }
        return true;
    }


    /**
     *
     * 处理发送指令逻辑
     *
     * @param request
     * @param localAddress
     * @return

     */
    private MessageResponse<?> sendCmd(MessageRequest<?> request, String localAddress){
        Message<?> message;
        String messageStr;
        String deviceNumber = request.getIme();
        String token = request.getToken();
        AuthPrincipal authPrincipal = AuthPrincipal.buildAndroidAuthPrincipal(token, deviceNumber);
        // 远程发送前本地尝试取数据
        MessageResponse<?> response = tryLoadByLocalCache(request, authPrincipal);
        if (response != null) {
            return response;
        }

        // 校验是否可以发送指令
        if (!canSend(request, authPrincipal)) {
            return MessageResponse.fastFailure(String.format("[%s]-[%s]指令未达到发送间隔或今日次数已超限！", deviceNumber, request.getCmd().name()));
        }


        if (!request.isRedirect()) {
            message = Message.request(request.getCmd(), request.getPayload(), request.getClientChannel());
            message.setLogicPrimaryKey(request.getLogicPrimaryKey());
            buildAccountInfoExtra(request, message, authPrincipal);
            messageStr = JsonUtil.asString(message);
        } else {
            // 该台机器并不能处理这个请求
            if (!localAddress.equals(request.getSocketSessionOn())) {
                log.info("连接不在本机，无法处理转发接口!【{}】", localAddress);
                return null;
            }
            messageStr = request.getMessage();
            message = JsonUtil.toBean(request.getMessage(), Message.class);
        }
        WebSocketSessionWrapper webSocketSessionWrapper = WebsocketSessionStorage.get(authPrincipal);
        if (webSocketSessionWrapper == null || WebSocketSessionWrapper.STATUS_OFF_LINE
                .equals(webSocketSessionWrapper.getStatus())) {
            MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
            if (baseDevice == null) {
                return MessageResponse.fastFailure("设备不存在！");
            }

            if (WebSocketSessionWrapper.STATUS_OFF_LINE.equals(baseDevice.getIsOnline())) {
                return MessageResponse.fastFailure("设备不在线！");
            }
            if (!request.isRedirect()) {
                log.info("设备连接在本机不存在，指令下发请求被转发..............");
                // 转发
                request.setRedirect(true);
                request.setRedirectFrom(localAddress);
                request.setSocketSessionOn(baseDevice.getConnectServerAddress());
                // 必须在接口的源头出生成请求对象，这样实际能处理业务的机器返回的数据才能使用request_id和这里对应起来
                request.setMessage(messageStr);
                log.info("广播指令下发数据: {}", request);
                redisTemplate.convertAndSend(WebsocketConst.REDIRECT_CMD_TOPIC, request);
                return blockUntilDataFlush(request, message.getRequestId(), request.isAsync(),
                        request.getBlockMilliSeconds());
            } else {
                return null;
            }
        }
        // 日志必须记录成功，才能发送消息；接收消息时如果没有对应的请求，则不会做业务处理
        channelTransferService.recordRequest(authPrincipal, messageStr, message, request);
        log.info("对设备[{}]下发指令: {}", deviceNumber, messageStr);
        WebsocketSessionStorage.sendMessage(authPrincipal, message);
        MessageResponse<?> messageResponse = blockUntilDataFlush(request, message.getRequestId(), request.isAsync(),
                request.getBlockMilliSeconds());
        if (request.isRedirect()) {
            log.info("本次接口请求为{}转发过来，数据处理完成，广播返回数据: {}", request.getRedirectFrom(), messageResponse);
            messageResponse.setRequestId(message.getRequestId());
            redisTemplate.convertAndSend(WebsocketConst.RETURN_MESSAGE_TOPIC, messageResponse);
        }
        return messageResponse;
    }

    /**
     * 对于某些命令阻塞至数据传输回来
     *
     * @param requestId 请求id，数据回传回来之后需要根据这个来对应起来
     * @param async     是否需要阻塞
     * @return

     */
    private MessageResponse<?> blockUntilDataFlush(@NotNull MessageRequest<?> messageRequest, @NotNull String requestId,
            boolean async, long blockMilliSeconds) {
        // 非单个设备的指令阻塞没有意义
        if (async || !MessageRequest.SendMode.SINGLE.equals(messageRequest.getSendMode())) {
            return MessageResponse.success(requestId);
        }
        Preconditions.checkNotNull(requestId, "请求id不能为空!");
        WebsocketSessionStorage.put(requestId);
        if (blockMilliSeconds <= 0) {
            return WebsocketSessionStorage.getResponse(requestId);
        }
        return WebsocketSessionStorage.getResponse(requestId, blockMilliSeconds);
    }

    /**
     * 根据请求对象判断是否需要附加支付方式的账号信息
     * @param request
     * @param message
     */
    private void buildAccountInfoExtra(MessageRequest<?> request, Message<?> message, AuthPrincipal authPrincipal) {
        if (request.isAppendAccountInfo()) {
            MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
        }
    }
}
