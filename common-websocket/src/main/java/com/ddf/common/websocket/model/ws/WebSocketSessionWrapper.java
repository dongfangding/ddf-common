package com.ddf.common.websocket.model.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.Serializable;
import java.security.Principal;

/**
 * 包装WebsocketSession
 *
 * @author dongfang.ding
 * @date 2019/8/20 18:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketSessionWrapper implements Serializable {

    /** 连接状态离线 */
    public static final Integer STATUS_OFF_LINE = 0;
    /** 连接状态在线 */
    public static final Integer STATUS_ON_LINE = 1;

    /**
     * 连接认证对象
     */
    private Principal authPrincipal;

    /**
     * ws的底层不允许并发发送消息，所以要么发送消息的方法是同步的，要么使用这个类来保证只有一个线程在发送消息
     */
    private ConcurrentWebSocketSessionDecorator webSocketSession;

    /**
     * 连接状态 0 离线 1在线
     */
    private Integer status;

    /**
     * 连接状态变化后需要与数据库做同步，加一个标识代表是否已同步过，如果已同步的话，则不会触发数据库更新
     */
    private boolean sync;

    /**
     * 连接状态最后一次变化的时间，单位毫秒
     */
    private long statusChangeTime;

    /**
     * 连接到服务器的地址
     */
    private String serverAddress;

    /**
     * 客户端地址
     */
    private String clientAddress;

}
