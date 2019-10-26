package com.ddf.common.websocket.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.common.websocket.model.entity.ChannelTransfer;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.MessageRequest;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;

import java.security.Principal;
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
     * @param values [AuthPrincipal该设备的认证，String 发送的内容，{@link Message}对象的json形式]
     * @param request
     * @return
     */
    Map<AuthPrincipal, String> batchRecordRequest(ConcurrentHashMap<Principal, WebSocketSessionWrapper> values, MessageRequest request);

    /**
     * 记录请求数据
     * @param authPrincipal
     * @param request
     * @param message
     * @param messageRequest
     * @return
     */
    boolean recordRequest(AuthPrincipal authPrincipal, String request, Message message, MessageRequest messageRequest);

    /**
     * 记录响应日志, 当message为空时说明序列化接收到的数据有问题，此时数据做插入备份
     * @param authPrincipal
     * @param requestId
     * @param response
     * @param message
     * @return -1 请求不存在 0 成功 1 重复请求
     */
    int recordResponse(AuthPrincipal authPrincipal, String requestId, String response, Message message);

    /**
     * 将处理状态更新为成功或失败
     * @param message
     * @param isSuccess
     * @param errorMessage
     * @param response
     * @param serverSend
     * @return
     */
    boolean updateToComplete(Message message, boolean isSuccess, String errorMessage, String response, String serverSend);
    
    /**
     * 根据requestId获取报文请求时的业务对象记录
     * 
     * @param requestId
     * @return
     * @author dongfang.ding
     * @date 2019/9/28 15:15 
     */
    String getPayloadByRequestId(String requestId);
}
