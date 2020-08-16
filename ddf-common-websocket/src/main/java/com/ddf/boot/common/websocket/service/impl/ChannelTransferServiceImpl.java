package com.ddf.boot.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.boot.common.core.exception.GlobalCustomizeException;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.exception.ClientRepeatRequestException;
import com.ddf.boot.common.websocket.helper.CmdAction;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.mapper.ChannelTransferMapper;
import com.ddf.boot.common.websocket.model.entity.ChannelTransfer;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.MessageRequest;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    @Transactional(rollbackFor = Exception.class)
    public Map<AuthPrincipal, String> batchRecordRequest(ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> values
            ,MessageRequest messageRequest) {
        if (values == null || messageRequest.getCmd() == null) {
            return null;
        }
        Map<AuthPrincipal, String> messageMap = new HashMap<>(values.size());
        List<ChannelTransfer> channelTransfers = new ArrayList<>(values.size());
        for (Map.Entry<AuthPrincipal, WebSocketSessionWrapper> entry : values.entrySet()) {
            WebSocketSessionWrapper value = entry.getValue();
            if (WebSocketSessionWrapper.STATUS_OFF_LINE.equals(value.getStatus())) {
                continue;
            }
            AuthPrincipal authPrincipal = entry.getKey();
            CmdAction cmdAction = new CmdAction();
            Message message = cmdAction.push(messageRequest.getCmd(), messageRequest.getPayload());
            String messageStr = JsonUtil.asString(message);
            channelTransfers.add(buildChannelTransfer(authPrincipal, message, messageStr, value, messageRequest));
            messageMap.put(authPrincipal, messageStr);
        }
        Lists.partition(channelTransfers, 500).forEach(ls-> saveBatch(ls));
        return messageMap;
    }

    private ChannelTransfer buildChannelTransfer(AuthPrincipal authPrincipal, Message message, String request
            ,WebSocketSessionWrapper webSocketSessionWrapper, MessageRequest messageRequest) {
        ChannelTransfer channelTransfer = new ChannelTransfer();
        channelTransfer.setCmd(message.getCmd().name());
        channelTransfer.setRequestId(message.getRequestId());
        channelTransfer.setRequest(request);
        channelTransfer.setFullRequestResponse(toJsonArr(request));
        channelTransfer.setSendFlag(ChannelTransfer.SEND_FLAG_SERVER);
        channelTransfer.setDeviceNumber(authPrincipal.getDeviceNumber());
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
            save(buildChannelTransfer(authPrincipal, message, request, webSocketSessionWrapper, messageRequest));
        } catch (Exception e) {
            throw new ClientRepeatRequestException(String.format("客户端重复对同一数据[%s]发送相同指令！",
                    messageRequest.getLogicPrimaryKey()));
        }
        return true;
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
            channelTransfer.setDeviceNumber(authPrincipal.getDeviceNumber());
            channelTransfer.setToken(authPrincipal.getToken());
            channelTransfer.setStatus(ChannelTransfer.STATUS_RECEIVED);
            if (message == null) {
                channelTransfer.setStatus(ChannelTransfer.STATUS_FAILURE);
            } else {
                channelTransfer.setRequestId(requestId);
                channelTransfer.setCmd(message.getCmd().name());
                LambdaQueryWrapper<ChannelTransfer> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(ChannelTransfer::getRequestId, requestId);
                queryWrapper.eq(ChannelTransfer::getRemoved, 0);
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
        queryWrapper.eq(ChannelTransfer::getRemoved, 0);
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
        updateWrapper.eq(ChannelTransfer::getRemoved, 0);
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
        updateWrapper.eq(ChannelTransfer::getRemoved, 0);
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
        queryWrapper.eq(ChannelTransfer::getRemoved, 0);
        queryWrapper.eq(ChannelTransfer::getRequestId, requestId);
        return getOne(queryWrapper);
    }


    /**
     * 根据requestId获取报文请求时的业务对象
     *
     * @param requestId
     * @return


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
     * 获取指定设备该指定上一次下发指令的历史数据
     *
     * @param deviceNumber
     * @param cmd
     * @return
     */
    @Override
    public ChannelTransfer getPreLog(String deviceNumber, String cmd) {
        if (StringUtils.isAnyBlank(deviceNumber, cmd)) {
            return null;
        }
        LambdaQueryWrapper<ChannelTransfer> channelTransferWrapper = Wrappers.lambdaQuery();
        channelTransferWrapper.eq(ChannelTransfer::getDeviceNumber, deviceNumber);
        channelTransferWrapper.eq(ChannelTransfer::getCmd, cmd);
        channelTransferWrapper.orderByDesc(ChannelTransfer::getCreateTime);
        channelTransferWrapper.last("limit 1");
        return getOne(channelTransferWrapper);
    }


    /**
     * 获取指定设备该指定上一次下发指令的历史数据,这里固定只查今天的数据，
     * 这样只要查一次就能满足今天的发送次数和最新的一次；如果今天没有，那么也需要知道上次的发送时间；肯定是要发送的，
     * 这里如果是牵扯到第一天和第二天短时间内时间跨度不满足发送间隔的话，我觉得这个问题可以忽略
     *
     * @param deviceNumber
     * @param cmd
     * @param successCount 是否只有成功的才计数
     * @return
     */
    @Override
    public List<ChannelTransfer> getTodayLog(String deviceNumber, String cmd, boolean successCount) {
        if (StringUtils.isAnyBlank(deviceNumber, cmd)) {
            return null;
        }
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        LambdaQueryWrapper<ChannelTransfer> channelTransferWrapper = Wrappers.lambdaQuery();
        channelTransferWrapper.eq(ChannelTransfer::getDeviceNumber, deviceNumber);
        channelTransferWrapper.eq(ChannelTransfer::getCmd, cmd);
        if (successCount) {
            // 有效次数为拿到成功的为基准，其实这个状态加上也会有一些问题，可能会导致次数超限啊，没有响应啊之类的
            channelTransferWrapper.eq(ChannelTransfer::getStatus, ChannelTransfer.STATUS_SUCCESS);
        }
        channelTransferWrapper.ge(ChannelTransfer::getCreateTime, calendar.getTime());
        channelTransferWrapper.orderByDesc(ChannelTransfer::getCreateTime);
        return list(channelTransferWrapper);
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
