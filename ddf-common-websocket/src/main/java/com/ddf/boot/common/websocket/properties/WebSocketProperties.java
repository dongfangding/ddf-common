package com.ddf.boot.common.websocket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/09/15 20:11
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.websocket.properties")
public class WebSocketProperties {

    /**
     * 最大文本数据包大小
     * @see org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean
     */
    private Integer maxTextMessageBufferSize = 8192;

    /**
     * 最大二进制数据包大小
     * @see org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean
     */
    private Integer maxBinaryMessageBufferSize = 8192;

    /**
     * 最大session空闲时间，超过空闲时间会断开连接,单位毫秒
     * @see org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean
     */
    private Long maxSessionIdleTimeout = 60000L;

    /**
     * 暴露的服务端点
     * @see WebSocketConfig#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry)
     */
    private String endPoint = "ddf-ws";

    /**
     * @see ConcurrentWebSocketSessionDecorator
     */
    private int sendTimeLimit = 3000;

    /**
     * @see ConcurrentWebSocketSessionDecorator
     */
    private int bufferSizeLimit = 102400;

    /**
     * 握手认证器， 系统提供一个默认的握手处理器的一个流程。如果不需要自定义实现，则默认的会生效
     * @see DefaultHandshakeInterceptor
     * @see WebSocketConfig#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry)
     */
    private List<HandshakeInterceptor> handshakeInterceptors;

    /**
     * 认证参数中的毫秒值有效毫秒值， 当认证参数中携带的时间戳与当前时间已经间隔大于这个值，则视为参数无效
     * @see DefaultHandshakeInterceptor#validArgument(com.ddf.boot.quick.websocket.model.HandshakeParam, org.springframework.http.server.ServerHttpResponse)
     */
    private long validAuthTimeStamp = 60 * 1000 * 5;

    /**
     * 是否忽略验证参数的时间戳，方便调试时可以改为true
     * @see DefaultHandshakeInterceptor#validArgument(com.ddf.boot.quick.websocket.model.HandshakeParam, org.springframework.http.server.ServerHttpResponse)
     */
    private boolean ignoreAuthTimestamp;


    /**
     * 握手时的认证参数是否加密
     * @see DefaultHandshakeInterceptor#beforeHandshake(org.springframework.http.server.ServerHttpRequest, org.springframework.http.server.ServerHttpResponse, org.springframework.web.socket.WebSocketHandler, java.util.Map)
     * @see WebSocketProperties#rsaPrivateKey
     */
    private boolean handshakeTokenSecret;

    /**
     * 是否加密传输消息
     * @see WebsocketSessionStorage#sendMessage(com.ddf.boot.quick.websocket.model.WebSocketSessionWrapper, com.ddf.boot.quick.websocket.model.Message)
     */
    private boolean messageSecret;

    /**
     * 同messageSecret和handshakeTokenSecret参数一起使用，如果要加密的话，提供了一个接口允许实现加解密算法
     * 提供了也给默认基于RSA的实现，也可以直接配置密钥即可， 如果要基于系统默认的RSA实现的话，私钥必须提供
     * @see com.ddf.boot.quick.websocket.util.WsSecureUtil
     * @see com.ddf.boot.quick.websocket.interceptor.EncryptProcessor
     * @see RSAEncryptProcessor
     */
    private String rsaPrivateKey;

    /**
     * 同样也是和messageSecret和handshakeTokenSecret配合使用的， 由于通用包默认实现了基于RSA的， 如果客户端也实现一套， 到时候服务端不知道要用哪个，所以如果
     * 存在多个实现， 需要配置具体实现的bean的代码
     */
    private String secretBeanName = "RSAEncryptProcessor";

    /**
     * 同messageSecret参数一起使用，如果要加密的话，提供了一个接口允许实现加解密算法
     * 提供了也给默认基于RSA的实现，也可以直接配置密钥即可
     *
     * 公钥可以不提供，这里只是为了方便调试，如果想直接使用提供的工具类进行调试，而公钥自己又有的话，可以配置进来进行调试，
     */
    private String rsaPublicKey;

    /**
     * 有时候服务端向客户端发送数据时会希望得到一些响应，而这个响应当前这个请求希望是可以同步得到的，这里提供一个默认最大阻塞时间.
     * 更为一种常见的场景是，服务端提供接口允许第三方发送数据给客户端， 然后索要数据，而这个时候第三方是希望同步的。
     * 当然具体请求可以自定义阻塞时间，但自定义的阻塞时间不能大于这个默认的最大阻塞时间，这是为了对应用的一个保护
     * @see com.ddf.boot.quick.websocket.model.MessageRequest
     * @see WebsocketSessionStorage#getResponse(String, long)
     */
    private long maxSyncBlockMillions = 600000;
}
