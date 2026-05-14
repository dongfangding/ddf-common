package com.ddf.boot.common.websocket.service;


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
 * @since 2019/8/23 9:45
 */
public interface ChannelTransferService {

    /**
     * 批量创建本机所有设备的消息记录
     *
     * @param values 参数值集合
     * @param request 请求对象
     */
    <T> Map<AuthPrincipal, String> batchRecordRequest(ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> values,
            MessageRequest<T> request);

    /**
     * 记录请求数据
     *
     * @param authPrincipal 认证主体对象
     * @param request 请求对象
     * @param message 消息内容
     * @param messageRequest 消息请求参数
     */
    <M, R> boolean recordRequest(AuthPrincipal authPrincipal, String request, Message<M> message,
            MessageRequest<R> messageRequest);

    /**
     * 记录响应日志, 当message为空时说明序列化接收到的数据有问题，此时数据做插入备份
     *
     * @param authPrincipal 认证主体对象
     * @param requestId 请求 ID
     * @param response 响应对象
     * @param message 消息内容
     * @return -1 请求不存在 0 成功 1 重复请求
     */
    <M> int recordResponse(AuthPrincipal authPrincipal, String requestId, String response, Message<M> message);

    /**
     * 将处理状态更新为成功或失败
     *
     * @param message 消息内容
     * @param isSuccess 是否success
     * @param errorMessage 错误消息参数
     * @param response 响应对象
     * @param serverSend 服务端send参数
     */
    <M> boolean updateToComplete(Message<M> message, boolean isSuccess, String errorMessage, String response,
            String serverSend);

    /**
     * 根据requestId获取报文请求时的业务对象记录
     *
     * @param requestId 请求 ID
     */
    String getPayloadByRequestId(String requestId);


    /**
     * 获取指定设备该指定上一次下发指令的历史数据
     *
     * @param accessKeyId 访问键ID
     * @param cmd 命令参数
     */
    ChannelTransfer getPreLog(String accessKeyId, String cmd);


    /**
     * 获取指定设备今天发送的某个指令的历史数据列表
     *
     * @param accessKeyId 访问键ID
     * @param cmd 命令参数
     * @param successCount 是否只查询成功的才计数
     */
    List<ChannelTransfer> getTodayLog(String accessKeyId, String cmd, boolean successCount);
}
