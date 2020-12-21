package com.ddf.boot.common.websocket.service;


import com.ddf.boot.common.websocket.model.MessageRequest;
import com.ddf.boot.common.websocket.model.MessageResponse;
import java.util.List;

/**
 * 通过接口服务向指定的客户端发送消息
 * <p>
 * <p>
 * 使用方可以将当前接口进行包装，或者是暴露http服务或者是rpc
 *
 * @author dongfang.ding
 * @date 2020-09-16
 */
public interface WsMessageService {

    /**
     * 执行指令
     *
     * @param request 请求参数
     * @return
     */
    <T, Q> MessageResponse<T> executeCmd(MessageRequest<Q> request);

    /**
     * 针对每个设备的数据不一样的批量发送接口指令
     *
     * @param requests
     * @return
     */
    <T, Q> MessageResponse<T> executeCmd(List<MessageRequest<Q>> requests);
}
