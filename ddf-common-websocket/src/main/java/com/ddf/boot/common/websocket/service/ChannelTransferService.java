package com.ddf.boot.common.websocket.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ChannelTransfer;
import com.ddf.boot.common.websocket.model.Message;
import com.ddf.boot.common.websocket.model.MessageRequest;
import com.ddf.boot.common.websocket.model.WebSocketSessionWrapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道传输service
 *
 * @author dongfang.ding
 * @date 2019/8/23 9:45
 */
public interface ChannelTransferService extends IService<ChannelTransfer> {

    /**
     * 批量创建本机所有设备的消息记录
     *
     * @param values  [AuthPrincipal该设备的认证，String 发送的内容，{@link Message}对象的json形式]
     * @param request
     * @return
     */
    <T> Map<AuthPrincipal, String> batchRecordRequest(ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> values,
            MessageRequest<T> request);

    /**
     * 记录请求数据
     *
     * @param authPrincipal
     * @param request
     * @param message
     * @param messageRequest
     * @return
     */
    <M, R> boolean recordRequest(AuthPrincipal authPrincipal, String request, Message<M> message,
            MessageRequest<R> messageRequest);

    /**
     * 记录响应日志, 当message为空时说明序列化接收到的数据有问题，此时数据做插入备份
     *
     * @param authPrincipal
     * @param requestId
     * @param response
     * @param message
     * @return -1 请求不存在 0 成功 1 重复请求
     */
    <M> int recordResponse(AuthPrincipal authPrincipal, String requestId, String response, Message<M> message);

    /**
     * 将处理状态更新为成功或失败
     *
     * @param message
     * @param isSuccess
     * @param errorMessage
     * @param response
     * @param serverSend
     * @return
     */
    <M> boolean updateToComplete(Message<M> message, boolean isSuccess, String errorMessage, String response,
            String serverSend);

    /**
     * 根据requestId获取报文请求时的业务对象记录
     *
     * @param requestId
     * @return
     */
    String getPayloadByRequestId(String requestId);


    /**
     * 获取指定设备该指定上一次下发指令的历史数据
     *
     * @param accessKeyId
     * @param cmd
     * @return
     */
    ChannelTransfer getPreLog(String accessKeyId, String cmd);


    /**
     * 获取指定设备今天发送的某个指令的历史数据列表
     *
     * @param accessKeyId
     * @param cmd
     * @param successCount 是否只查询成功的才计数
     * @return
     */
    List<ChannelTransfer> getTodayLog(String accessKeyId, String cmd, boolean successCount);
}
