package com.ddf.boot.common.websocket.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.WebUtil;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.enumu.CacheKeyEnum;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.interceptor.WsMessageFilter;
import com.ddf.boot.common.websocket.model.*;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.WsMessageService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 提供rest接口允许对在线的客户端发送消息
 *
 * @author dongfang.ding
 * @date 2020-09-16
 */
@Slf4j
@Service
public class WsMessageServiceImpl implements WsMessageService {
    @Autowired(required = false)
    private ChannelTransferService channelTransferService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private Environment environment;
    @Autowired
    private ThreadPoolTaskExecutor batchCmdExecutor;
    @Autowired(required = false)
    private List<WsMessageFilter> wsMessageFilters;


    /**
     * 批量发送指令，该方法目前只支持异步
     *
     * @param requestList
     * @date 2019/10/18 9:49
     */
    @Override
    public <T, Q> MessageResponse<T> executeCmd(List<MessageRequest<Q>> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return MessageResponse.fastFailure("参数不能为空！");
        }
        for (MessageRequest<?> messageRequest : requestList) {
            batchCmdExecutor.execute(() -> executeCmd(messageRequest));
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
    public <T, Q> MessageResponse<T> executeCmd(MessageRequest<Q> request) {
        try {
            MessageResponse<T> validResponse = validRequiredParam(request);
            if (validResponse != null) {
                return validResponse;
            }

            boolean filter = filter(request);
            if (!filter) {
                return MessageResponse.unauthorized();
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
            // TODO 处理群发机器转发
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
                return sendCmd(request, localAddress);
            }
        } catch (Exception e) {
            log.error("指令下发失败！指令参数: [{}]", request, e);
            return MessageResponse.fastFailure(e.getMessage());
        }
    }


    /**
     * 之前过滤器校验，有一个校验不通过，则不会继续执行
     * @param request
     * @param <Q>
     * @return
     */
    private <Q> boolean filter(MessageRequest<Q> request) {
        if (CollUtil.isNotEmpty(wsMessageFilters)) {
            for (WsMessageFilter wsMessageFilter : wsMessageFilters) {
                boolean filter = wsMessageFilter.filter(request);
                if (!filter) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 必传参数校验
     * @param request
     * @param <T>
     * @param <Q>
     * @return
     */
    private <T, Q> MessageResponse<T> validRequiredParam(MessageRequest<Q> request) {
        if (request == null || request.getCmd() == null) {
            return MessageResponse.fastFailure("指令不能为空!");
        }
        if (request.getSendMode() == null) {
            return MessageResponse.fastFailure("请传入指令的发送模式， SINGLE, BATCH, ALL!");
        }
        if (request.getLoginType() == null) {
            String format = String.format("请传入认证身份的登录方式[%s]", Arrays.toString(AuthPrincipal.LoginType.values()));
            return MessageResponse.fastFailure(format);
        }
        return null;
    }

    /**
     * 尝试从本地缓存获取数据
     * @param request
     * @return
     */
    private <T, Q> MessageResponse<T> tryLoadByLocalCache(MessageRequest<Q> request, AuthPrincipal authPrincipal) {
        try {
        } catch (Exception e) {
            log.error("处理指令[{}]发送前检查失败！", request);
        }
        return null;
    }

    /**
     * 校验是否可以发送指令
     * @param request
     * @param authPrincipal
     * @return
     */
    private <Q> boolean canSend(MessageRequest<Q> request, AuthPrincipal authPrincipal) {
        if (request.isCheckLastTime()) {
            // 如果需要检查上次发送时间，则必须在指定的间隔时间之后
            List<ChannelTransfer> preLogs = channelTransferService.getTodayLog(authPrincipal.getAccessKeyId(), request.getCmd(), true);
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
    private <T, Q> MessageResponse<T> sendCmd(MessageRequest<Q> request, String localAddress){
        Message<?> message;
        String messageStr;
        String accessKeyId = request.getAccessKeyId();
        String authCode = request.getAuthCode();
        AuthPrincipal authPrincipal = AuthPrincipal.buildChannelPrincipal(accessKeyId, authCode, request.getLoginType());
        // 远程发送前本地尝试取数据
        MessageResponse<T> response = tryLoadByLocalCache(request, authPrincipal);
        if (response != null) {
            return response;
        }

        // 校验是否可以发送指令
        if (!canSend(request, authPrincipal)) {
            return MessageResponse.fastFailure(String.format("[%s]-[%s]指令未达到发送间隔或今日次数已超限！", accessKeyId, request.getCmd()));
        }

        if (!request.isRedirect()) {
            message = Message.request(request.getCmd(), request.getClientChannel(), request.getPayload());
            message.setLogicPrimaryKey(request.getLogicPrimaryKey());
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

            String monitorJson = (String) redisTemplate.opsForHash().get(CacheKeyEnum.AUTH_PRINCIPAL_SERVER_MONITOR.getTemplate(),
                    MessageFormat.format(CacheKeyEnum.AUTH_PRINCIPAL_MONITOR.getTemplate(),
                            request.getLoginType(), request.getAccessKeyId(), request.getAuthCode()));
            if (StringUtils.isBlank(monitorJson)) {
                return MessageResponse.fastFailure("设备不存在！");
            }
            webSocketSessionWrapper = JsonUtil.toBean(monitorJson, WebSocketSessionWrapper.class);
            if (WebSocketSessionWrapper.STATUS_OFF_LINE.equals(webSocketSessionWrapper.getStatus())) {
                return MessageResponse.fastFailure("设备不在线！");
            }
            if (!request.isRedirect()) {
                log.info("设备连接在本机不存在，指令下发请求被转发..............");
                // 转发
                request.setRedirect(true);
                request.setRedirectFrom(localAddress);
                request.setSocketSessionOn(webSocketSessionWrapper.getServerAddress());
                // 必须在接口的源头出生成请求对象，这样实际能处理业务的机器返回的数据才能使用request_id和这里对应起来
                request.setMessage(messageStr);
                log.info("广播指令下发数据: {}", request);
                redisTemplate.convertAndSend(WebsocketConst.REDIRECT_CMD_TOPIC, JsonUtil.asString(request));
                return blockUntilDataFlush(request, message.getRequestId(), request.isAsync(),
                        request.getBlockMilliSeconds());
            } else {
                return null;
            }
        }
        // 取出更详细的存储的数据
        authPrincipal = webSocketSessionWrapper.getAuthPrincipal();

        // 日志必须记录成功，才能发送消息；接收消息时如果没有对应的请求，则不会做业务处理
        if (channelTransferService != null) {
            channelTransferService.recordRequest(authPrincipal, messageStr, message, request);
        }
        log.info("对设备[{}]下发指令: {}", accessKeyId, messageStr);
        WebsocketSessionStorage.sendMessage(authPrincipal, message);
        MessageResponse<T> messageResponse = blockUntilDataFlush(request, message.getRequestId(), request.isAsync(),
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
    private <T, Q> MessageResponse<T> blockUntilDataFlush(@NotNull MessageRequest<Q> messageRequest, @NotNull String requestId,
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
}
