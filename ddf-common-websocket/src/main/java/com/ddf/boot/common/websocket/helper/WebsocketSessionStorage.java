package com.ddf.boot.common.websocket.helper;

import com.ddf.boot.common.util.SpringContextHolder;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.exception.ClientMessageCodeException;
import com.ddf.boot.common.websocket.exception.SocketSendException;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.MessageResponse;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维护建立到服务端的websocket连接
 * <p>
 *
 * @author dongfang.ding
 * @date 2019/8/20 18:38
 */
@Slf4j
public class WebsocketSessionStorage {

    private static MerchantBaseDeviceService merchantBaseDeviceService = SpringContextHolder.getBean(MerchantBaseDeviceService.class);
    private static Environment environment = SpringContextHolder.getBean(Environment.class);

    /**
     * 连接对象
     */
    public static final ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> WEB_SOCKET_SESSION_MAP = new ConcurrentHashMap<>();


    /**
     * 由于下发指令和实际上的数据响应时异步的，因此提供一个阻塞的实现，两个线程来操作同一个对象来实现阻塞至数据到达
     */
    private static final ConcurrentHashMap<String, MessageResponse> REQUEST_CONNECT_RESPONSE_MAP = new ConcurrentHashMap<>(1000);


    /**
     * 返回当前认证身份对应的WebSocketSession
     *
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     */
    public static WebSocketSessionWrapper get(AuthPrincipal authPrincipal) {
        return WEB_SOCKET_SESSION_MAP.get(authPrincipal);
    }

    /**
     * 认证身份用户对应的WebSocketSession在线
     *
     * @param authPrincipal
     * @param webSocketSession
     * @author dongfang.ding
     */
    public static void active(AuthPrincipal authPrincipal, WebSocketSession webSocketSession) {
        WebSocketSessionWrapper webSocketSessionWrapper = new WebSocketSessionWrapper(authPrincipal, new ConcurrentWebSocketSessionDecorator(
                webSocketSession, WebsocketConst.SEND_TIME_LIMIT, WebsocketConst.BUFFER_SIZE_LIMIT),
                WebSocketSessionWrapper.STATUS_ON_LINE, false, System.nanoTime(),
                webSocketSession.getAttributes().get(WebsocketConst.SERVER_IP) + ":" +
                        environment.getProperty("server.port"),
                webSocketSession.getAttributes().get(WebsocketConst.CLIENT_REAL_IP) + "");
        merchantBaseDeviceService.sync(authPrincipal, webSocketSessionWrapper);
    }

    /**
     * 认证身份用户对应的WebSocketSession离线
     *
     * @param authPrincipal
     * @author dongfang.ding
     */
    public static void inactive(AuthPrincipal authPrincipal, WebSocketSession webSocketSession) {
        if (AuthPrincipal.LoginType.ANDROID.equals(authPrincipal.getLoginType())) {
            modifyStatus(authPrincipal, WebSocketSessionWrapper.STATUS_OFF_LINE, webSocketSession);
            merchantBaseDeviceService.sync(authPrincipal, get(authPrincipal));
        }
    }

    /**
     * 清除认证用户身份对应的WebSocketSession,清除前应保证离线状态已更新到表中
     *
     * @param authPrincipal
     * @author dongfang.ding
     */
    public static void remove(AuthPrincipal authPrincipal) {
        WEB_SOCKET_SESSION_MAP.remove(authPrincipal);
    }

    /**
     * 获取所有连接信息
     *
     * @return
     * @author dongfang.ding
     */
    public static ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> getAll() {
        return WEB_SOCKET_SESSION_MAP;
    }


    /**
     * 判断指定认证的可用连接是否在本机
     *
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     * @date 2019/9/24 15:17
     */
    public static boolean isSocketSessionOn(AuthPrincipal authPrincipal) {
        return WEB_SOCKET_SESSION_MAP.containsKey(authPrincipal);
    }


    /**
     * 修改状态节点
     *
     * @param authPrincipal
     * @param status
     * @return
     * @author dongfang.ding
     */
    public static boolean modifyStatus(AuthPrincipal authPrincipal, Integer status, WebSocketSession webSocketSession) {
        WebSocketSessionWrapper webSocketSessionWrapper = get(authPrincipal);
        // 由于服务端对客户端下线感知的延迟性，如果在感知之前重新上线，依然会触发服务端的下线时间
        if (webSocketSessionWrapper != null && webSocketSessionWrapper.getWebSocketSession().getDelegate() == webSocketSession) {
            webSocketSessionWrapper.setStatus(status);
            webSocketSessionWrapper.setStatusChangeTime(System.nanoTime());
            webSocketSessionWrapper.setSync(false);
            if (WebSocketSessionWrapper.STATUS_OFF_LINE.equals(status)) {
                webSocketSessionWrapper.setServerAddress(null);
            }
            WEB_SOCKET_SESSION_MAP.put(authPrincipal, webSocketSessionWrapper);
            return true;
        }
        return false;
    }

    /**
     * 修改同步状态
     *
     * @param authPrincipal
     * @param sync
     * @return
     * @author dongfang.ding
     */
    public static boolean modifySync(AuthPrincipal authPrincipal, boolean sync) {
        WebSocketSessionWrapper webSocketSessionWrapper = get(authPrincipal);
        if (webSocketSessionWrapper != null) {
            webSocketSessionWrapper.setSync(sync);
            WEB_SOCKET_SESSION_MAP.put(authPrincipal, webSocketSessionWrapper);
            return true;
        }
        return false;
    }


    /**
     * 将请求id放入对象中，等待填充
     *
     * @param requestId
     * @author dongfang.ding
     */
    public static void put(@NotNull String requestId) {
        Objects.requireNonNull(requestId, "请求id不能为空!");
        REQUEST_CONNECT_RESPONSE_MAP.put(requestId, MessageResponse.none());
    }


    /**
     * 业务处理类如果没有放入响应，则放入默认的响应
     *
     * @param message
     * @param response
     * @return
     * @author dongfang.ding
     * @date 2019/9/26 21:21
     */
    public static void putDefaultResponse(@NotNull Message message, @NotNull MessageResponse response) {
        if (!CmdEnum.PING.equals(message.getCmd()) && WebsocketSessionStorage.isNone(message.getRequestId())) {
            putResponse(message.getRequestId(), response);
        }
    }


    /**
     * 将响应的数据填充到requestId
     *
     * @param requestId
     * @param response
     * @author dongfang.ding
     */
    public static void putResponse(@NotNull String requestId, @NotNull MessageResponse response) {
        Objects.requireNonNull(requestId, "请求id不能为空!");
        if (responseIsTake(requestId)) {
            return;
        }
        Objects.requireNonNull(response, "响应数据不能为空!");
        log.info("放入[{}]响应数据: {}", requestId, response);
        response.setRequestId(requestId);
        REQUEST_CONNECT_RESPONSE_MAP.put(requestId, response);
    }

    /**
     * 针对客户端的响应判断是否响应成功，如果没有成功则将响应结果返回给调用者
     *
     * @param message
     * @return
     * @author dongfang.ding
     */
    public static boolean checkResponseIsSuccess(@NotNull Message message) {
        Objects.requireNonNull(message, "message不能为空!");
        Objects.requireNonNull(message.getRequestId(), "请求id不能为空!");
        if (Message.Type.REQUEST.equals(message.getType())) {
            return true;
        }
        if (!MessageResponse.SERVER_CODE_COMPLETE.equals(message.getCode())) {
            MessageResponse messageResponse;
            if (message.getBody() != null && StringUtils.isNotBlank(message.getBody().toString())) {
                messageResponse = MessageResponse.failure(MessageResponse.SERVER_CODE_ERROR,
                        message.getBody().toString());
            } else {
                messageResponse = MessageResponse.failure(MessageResponse.SERVER_CODE_ERROR,
                        String.format("客户端针对请求[%s]响应了非成功状态码[%s]，但是没有告诉我原因^_^",
                                message.getRequestId(), message.getCode()));
            }
            WebsocketSessionStorage.putResponse(message.getRequestId(), messageResponse);
            throw new ClientMessageCodeException(messageResponse.getMessage());
        }
        return true;
    }


    /**
     * 获取响应,如果超过阻塞时间则视为失败
     *
     * @param requestId
     * @param blockMilliSeconds
     * @return
     * @author dongfang.ding
     */
    public static MessageResponse getResponse(@NotNull String requestId, long blockMilliSeconds) {
        long initTime = System.currentTimeMillis();
        if (blockMilliSeconds > 30000) {
            blockMilliSeconds = 30000;
        }
        while (REQUEST_CONNECT_RESPONSE_MAP.get(requestId) == MessageResponse.none()) {
            try {
                if (System.currentTimeMillis() - initTime > blockMilliSeconds) {
                    return MessageResponse.delay(requestId);
                }
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        MessageResponse response = REQUEST_CONNECT_RESPONSE_MAP.get(requestId);
        log.info("请求id{}阻塞{}毫秒拿到响应数据{}=>", requestId, System.currentTimeMillis() - initTime, response);
        REQUEST_CONNECT_RESPONSE_MAP.remove(requestId);
        return response;
    }

    /**
     * 判断响应值是否已经被取走
     *
     * @param requestId
     * @return
     * @author dongfang.ding
     * @date 2019/9/26 18:39
     */
    public static boolean responseIsTake(String requestId) {
        return REQUEST_CONNECT_RESPONSE_MAP.get(requestId) == null;
    }

    /**
     * 是否没有放入过数据
     *
     * @param requestId
     * @return
     * @author dongfang.ding
     * @date 2019/9/26 20:35
     */
    public static boolean isNone(String requestId) {
        return REQUEST_CONNECT_RESPONSE_MAP.get(requestId) == MessageResponse.none();
    }


    /**
     * 获取响应,如果超过阻塞时间则视为失败,默认5秒
     *
     * @param requestId
     * @return
     * @author dongfang.ding
     */
    public static MessageResponse getResponse(@NotNull String requestId) {
        return getResponse(requestId, 10000);
    }


    /**
     * 返回存放请求对象
     *
     * @return
     * @author dongfang.ding
     */
    public static ConcurrentHashMap<String, MessageResponse> getRequestConnectResponseMap() {
        return REQUEST_CONNECT_RESPONSE_MAP;
    }


    /**
     * 发送数据
     * <p>
     * FIXME 一个连接发送过来数据，如果还没来得及处理给响应，这时候客户端断线了，那么下次重连的时候，
     * 连到另外一台机器，就会出现问题
     *
     * @param webSocketSessionWrapper
     * @param message
     * @author dongfang.ding
     */
    public static WebSocketSessionWrapper sendMessage(WebSocketSessionWrapper webSocketSessionWrapper, Message message) {
        if (webSocketSessionWrapper == null || webSocketSessionWrapper.getWebSocketSession() == null
                || !webSocketSessionWrapper.getWebSocketSession().isOpen()) {
            throw new SocketSendException("连接不可用！！");
        }
        try {
            AuthPrincipal authPrincipal = webSocketSessionWrapper.getAuthPrincipal();
            TextMessage textMessage = Message.wrapper(message);
            log.info("向[{}]-[{}]发送数据：{}", authPrincipal.getIme(), authPrincipal.getRandomCode(), textMessage.getPayload());
            TextMessage secretMessage = Message.wrapperWithSign(message);
            log.info("向[{}]-[{}]发送加密数据：{}", authPrincipal.getIme(), authPrincipal.getRandomCode(), secretMessage.getPayload());
            webSocketSessionWrapper.getWebSocketSession().sendMessage(textMessage);
        } catch (IOException e) {
            log.error("socket发送数据失败", e);
            throw new SocketSendException(e);
        }
        return webSocketSessionWrapper;
    }

    /**
     * 发送数据
     *
     * @param authPrincipal
     * @param message
     * @author dongfang.ding
     */
    public static WebSocketSessionWrapper sendMessage(AuthPrincipal authPrincipal, Message message) {
        WebSocketSessionWrapper webSocketSessionWrapper = get(authPrincipal);
        return sendMessage(webSocketSessionWrapper, message);
    }


    /**
     * 发送数据之后关闭连接
     *
     * @param webSocketSessionWrapper
     * @param message
     * @author dongfang.ding
     */
    public static void sendMessageAndClose(WebSocketSessionWrapper webSocketSessionWrapper, Message message) {
        try {
            sendMessage(webSocketSessionWrapper, message).getWebSocketSession().close();
        } catch (IOException e) {
            log.error("socket发送数据失败", e);
            throw new SocketSendException(e);
        }
    }


    /**
     * 发送数据之后关闭连接
     *
     * @param authPrincipal
     * @param message
     * @author dongfang.ding
     */
    public static void sendMessageAndClose(AuthPrincipal authPrincipal, Message message) {
        sendMessageAndClose(get(authPrincipal), message);
    }

}
