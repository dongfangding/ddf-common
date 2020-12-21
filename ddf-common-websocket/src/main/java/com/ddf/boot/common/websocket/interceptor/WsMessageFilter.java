package com.ddf.boot.common.websocket.interceptor;


import com.ddf.boot.common.websocket.model.MessageRequest;

/**
 * <p>消息发送过滤器</p >
 * <p>
 * 可以统一在发送消息前，对消息的目标进行一些类似权限校验或者什么之类的，决定目标客户端是否可以接受数据
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/09/16 15:29
 */
public interface WsMessageFilter {

    /**
     * 针对发送的参数进行校验，决定是否可以继续发送消息
     * 返回true,则继续执行发送逻辑；返回false, 则中断请求
     *
     * @param request
     * @param <Q>
     * @return
     */
    <Q> boolean filter(MessageRequest<Q> request);
}
