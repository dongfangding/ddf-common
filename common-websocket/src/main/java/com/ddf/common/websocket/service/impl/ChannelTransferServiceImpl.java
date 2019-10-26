package com.ddf.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.util.JsonUtil;
import com.ddf.common.websocket.enumerate.CmdEnum;
import com.ddf.common.websocket.exception.ClientRepeatRequestException;
import com.ddf.common.websocket.helper.CmdAction;
import com.ddf.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.common.websocket.mapper.ChannelTransferMapper;
import com.ddf.common.websocket.model.entity.ChannelTransfer;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.MessageRequest;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.common.websocket.service.ChannelTransferService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道传输接口实现
 *
 * @author dongfang.ding
 * @date 2019/8/23 9:45
 */
@Service
public class ChannelTransferServiceImpl extends ServiceImpl<ChannelTransferMapper, ChannelTransfer> implements ChannelTransferService {

    /**
     * 批量创建本机所有设备的消息记录
     *
     * @param values         [AuthPrincipal该设备的认证，String 发送的内容，{@link Message}对象的json形式]
     * @param messageRequest
     * @return
     */
    @Override
    public Map<AuthPrincipal, String> batchRecordRequest(ConcurrentHashMap<Principal, WebSocketSessionWrapper> values,
                                                         MessageRequest messageRequest) {
        if (values == null || messageRequest.getCmd() == null) {
            return null;
        }
        Map<AuthPrincipal, String> messageMap = new HashMap<>(values.size());
        List<ChannelTransfer> channelTransfers = new ArrayList<>(values.size());
        for (Map.Entry<Principal, WebSocketSessionWrapper> entry : values.entrySet()) {
            WebSocketSessionWrapper value = entry.getValue();
            if (WebSocketSessionWrapper.STATUS_OFF_LINE.equals(value.getStatus())) {
                continue;
            }
            AuthPrincipal authPrincipal = (AuthPrincipal) entry.getKey();
            CmdAction cmdAction = new CmdAction();
            Message message = cmdAction.push(messageRequest.getCmd(), messageRequest.getPayload());
            String messageStr = JsonUtil.asString(message);
            channelTransfers.add(buildChannelTransfer(authPrincipal, message, messageStr, value, messageRequest));
            messageMap.put(authPrincipal, messageStr);
        }
        saveBatch(channelTransfers);
        return messageMap;
    }

    /**
     * 记录请求数据
     *
     * @param authPrincipal
     * @param request
     * @param message
     * @param messageRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean recordRequest(AuthPrincipal authPrincipal, String request, Message message, MessageRequest messageRequest) {
        if (CmdEnum.PING.equals(message.getCmd())) {
            return true;
        }
        if (authPrincipal == null || StringUtils.isAnyBlank(request, message.getRequestId())) {
            return false;
        }
        WebSocketSessionWrapper webSocketSessionWrapper = WebsocketSessionStorage.get(authPrincipal);
        if (webSocketSessionWrapper == null) {
            return false;
        }
        try {
            // FIXME 由一个业务主键来维系唯一索引保证针对同一个业务数据的命令不能重复发送，然后手写sql使用insert ignore如果插入
            // 返回行数为0说明索引重复了，则可以明确知道是重复请求。这里后面再改，只要保存出错，都先认为是重复请求。
            save(buildChannelTransfer(authPrincipal, message, request, webSocketSessionWrapper, messageRequest));
        } catch (Exception e) {
            throw new ClientRepeatRequestException(String.format("客户端重复对同一数据[%s]发送相同指令！",
                    messageRequest.getLogicPrimaryKey()));
        }
        return true;
    }

    private ChannelTransfer buildChannelTransfer(AuthPrincipal authPrincipal, Message message, String request,
                                                 WebSocketSessionWrapper webSocketSessionWrapper, MessageRequest messageRequest) {
        ChannelTransfer channelTransfer = new ChannelTransfer();
        channelTransfer.setCmd(message.getCmd().name());
        channelTransfer.setRequestId(message.getRequestId());
        channelTransfer.setRequest(request);
        channelTransfer.setFullRequestResponse(toJsonArr(request));
        channelTransfer.setSendFlag(ChannelTransfer.SEND_FLAG_SERVER);
        channelTransfer.setDeviceNumber(authPrincipal.getIme());
        channelTransfer.setToken(authPrincipal.getToken());
        channelTransfer.setStatus(ChannelTransfer.STATUS_SEND);
        if (messageRequest != null) {
            channelTransfer.setBusinessData(JsonUtil.asString(message.getBody()));
            channelTransfer.setOperatorId(messageRequest.getOperatorId());
            channelTransfer.setCreateBy(messageRequest.getOperatorId());
            channelTransfer.setLogicPrimaryKey(messageRequest.getLogicPrimaryKey());

        }
        channelTransfer.setServerAddress(webSocketSessionWrapper.getServerAddress());
        channelTransfer.setClientAddress(webSocketSessionWrapper.getClientAddress());
        return channelTransfer;
    }

    /**
     * 记录响应日志
     *
     * @param authPrincipal
     * @param requestId
     * @param response
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public int recordResponse(AuthPrincipal authPrincipal, String requestId, String response, Message message) {
        if (message != null && (CmdEnum.PING.equals(message.getCmd()) || CmdEnum.PONG.equals(message.getCmd()))) {
            // ignore
            return 0;
        }
        if (StringUtils.isBlank(response)) {
            return -1;
        }
        WebSocketSessionWrapper webSocketSessionWrapper = WebsocketSessionStorage.get(authPrincipal);
        if (webSocketSessionWrapper == null) {
            return -1;
        }
        ChannelTransfer channelTransfer = new ChannelTransfer();
        // 数据转换失败，则无法获取requestId，对这种数据做插入备份
        if (message == null || Message.Type.REQUEST.equals(message.getType())) {
            channelTransfer.setRequest(response);
            channelTransfer.setFullRequestResponse(toJsonArr(response));
            channelTransfer.setSendFlag(ChannelTransfer.SEND_FLAG_CLIENT);
            channelTransfer.setDeviceNumber(authPrincipal.getIme());
            channelTransfer.setToken(authPrincipal.getToken());
            channelTransfer.setStatus(ChannelTransfer.STATUS_RECEIVED);
            if (message == null) {
                channelTransfer.setStatus(ChannelTransfer.STATUS_FAILURE);
            } else {
                channelTransfer.setRequestId(requestId);
                channelTransfer.setCmd(message.getCmd().name());
                LambdaQueryWrapper<ChannelTransfer> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(ChannelTransfer::getRequestId, requestId);
                ChannelTransfer exist = getOne(queryWrapper);
                if (exist != null) {
                    return 1;
                }
            }
            channelTransfer.setServerAddress(webSocketSessionWrapper.getServerAddress());
            channelTransfer.setClientAddress(webSocketSessionWrapper.getClientAddress());
            save(channelTransfer);
            return 0;
        }
        if (StringUtils.isAnyBlank(requestId)) {
            return -1;
        }
        LambdaQueryWrapper<ChannelTransfer> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChannelTransfer::getRequestId, requestId);
        ChannelTransfer exist = getOne(queryWrapper);
        if (exist == null) {
            return -1;
        }
        if (!exist.getStatus().equals(ChannelTransfer.STATUS_SEND) && !exist.getStatus().equals(ChannelTransfer.STATUS_FAILURE)) {
            return 1;
        }
        LambdaUpdateWrapper<ChannelTransfer> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(ChannelTransfer::getResponse, response);
        updateWrapper.set(ChannelTransfer::getFullRequestResponse, appendJsonArr(exist.getFullRequestResponse(), response));
        updateWrapper.set(ChannelTransfer::getStatus, ChannelTransfer.STATUS_RECEIVED);
        updateWrapper.eq(ChannelTransfer::getId, exist.getId());
        update(null, updateWrapper);
        return 0;
    }

    /**
     * 将处理状态更新为成功或失败
     *
     * @param message
     * @param isSuccess
     * @param errorMessage
     * @param response
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateToComplete(Message message, boolean isSuccess, String errorMessage, String response
            , String serverSend) {
        if (message == null || StringUtils.isBlank(message.getRequestId())) {
            return false;
        }
        if (CmdEnum.PING.equals(message.getCmd())) {
            return true;
        }
        LambdaUpdateWrapper<ChannelTransfer> updateWrapper = Wrappers.lambdaUpdate();
        if (isSuccess) {
            updateWrapper.set(ChannelTransfer::getStatus, ChannelTransfer.STATUS_SUCCESS);
        } else {
            updateWrapper.set(ChannelTransfer::getStatus, ChannelTransfer.STATUS_FAILURE);
        }
        if (errorMessage != null && errorMessage.length() > 1000) {
            errorMessage = errorMessage.substring(0, 1000);
        }
        updateWrapper.set(ChannelTransfer::getErrorMessage, errorMessage);
        updateWrapper.eq(ChannelTransfer::getRequestId, message.getRequestId());
        if (response != null) {
            updateWrapper.set(ChannelTransfer::getResponse, response);
            ChannelTransfer exist = getByRequestId(message.getRequestId());
            if (exist != null) {
                updateWrapper.set(ChannelTransfer::getFullRequestResponse, appendJsonArr(exist.getFullRequestResponse(),
                        response, serverSend));
            }
        }
        return update(null, updateWrapper);
    }

    private ChannelTransfer getByRequestId(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            return null;
        }
        LambdaQueryWrapper<ChannelTransfer> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChannelTransfer::getRequestId, requestId);
        return getOne(queryWrapper);
    }


    /**
     * 根据requestId获取报文请求时的业务对象
     *
     * @param requestId
     * @return
     * @author dongfang.ding
     * @date 2019/9/28 15:15
     */
    @Override
    public String getPayloadByRequestId(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            throw new GlobalCustomizeException("requestId不存在，无法处理业务!");
        }

        LambdaQueryWrapper<ChannelTransfer> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChannelTransfer::getRequestId, requestId);
        ChannelTransfer record = getOne(queryWrapper);
        if (record == null) {
            throw new GlobalCustomizeException(String.format("【%s】没有对应的日志记录，无法处理！", requestId));
        }
        if (StringUtils.isBlank(record.getBusinessData())) {
            throw new GlobalCustomizeException(String.format("日志【%s】中的业务对象数据丢失！", requestId));
        }
        return record.getBusinessData();
    }

    /**
     * 对完整报文做特殊处理
     *
     * @param content
     * @return
     */
    private static String toJsonArr(String... content) {
        if (content == null || content.length == 0) {
            return "[]";
        }
        return "[" + StringUtils.join(content, ",") + "]";
    }


    /**
     * 通过字符串拼接的方式将最新的报文放入完整保温数组中
     *
     * @param oldValue
     * @param content
     * @return
     */
    private static String appendJsonArr(String oldValue, String... content) {
        if (StringUtils.isBlank(oldValue)) {
            return toJsonArr(content);
        }
        if (content == null || content.length == 0) {
            return oldValue;
        }
        return oldValue.substring(0, oldValue.length() - 1) + "," + StringUtils.join(content, ",") + "]";
    }
}
