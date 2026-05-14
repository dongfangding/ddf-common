package com.ddf.boot.common.websocket.config;


import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.websocket.handler.CustomizeHandshakeHandler;
import com.ddf.boot.common.websocket.handler.DefaultWebSocketHandler;
import com.ddf.boot.common.websocket.handler.HandlerMessageService;
import com.ddf.boot.common.websocket.handler.impl.HandlerMessageServiceImpl;
import com.ddf.boot.common.websocket.helper.CmdStrategyHelper;
import com.ddf.boot.common.websocket.interceptor.DefaultHandshakeInterceptor;
import com.ddf.boot.common.websocket.interceptor.HandshakeAuth;
import com.ddf.boot.common.websocket.interceptor.RSAEncryptProcessor;
import com.ddf.boot.common.websocket.interceptor.WsMessageFilter;
import com.ddf.boot.common.websocket.listeners.RedirectCmdListener;
import com.ddf.boot.common.websocket.listeners.RemoveOfflineKeyListener;
import com.ddf.boot.common.websocket.listeners.WebSocketHandlerListener;
import com.ddf.boot.common.websocket.listeners.ServerNodeOfflineListener;
import com.ddf.boot.common.websocket.properties.WebSocketProperties;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.WsMessageService;
import com.ddf.boot.common.websocket.service.impl.ChannelTransferServiceImpl;
import com.ddf.boot.common.websocket.service.impl.WsMessageServiceImpl;
import java.util.List;
import java.util.Optional;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


/**
 * ws的配置类
 * 参考 https://docs.spring.io/spring/docs/5.1.9.RELEASE/spring-framework-reference/web.html#websocket
 * <p>
 * 当前项目未定型stomp over websocket
 *
 * @author dongfang.ding
 * @since 2019/8/20 11:43
 */
@AutoConfiguration
@EnableWebSocket
@MapperScan(basePackages = "com.ddf.boot.common.websocket.mapper")
@EnableConfigurationProperties(WebSocketProperties.class)
@Import(WebsocketThreadConfig.class)
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketProperties webSocketProperties;

    private final List<HandshakeAuth> handshakeAuthList;

    private final HandlerMessageService handlerMessageService;

    private final WebSocketHandlerListener webSocketHandlerListener;

    public WebSocketConfig(WebSocketProperties webSocketProperties, List<HandshakeAuth> handshakeAuthList,
            Optional<HandlerMessageService> handlerMessageService,
            Optional<WebSocketHandlerListener> webSocketHandlerListener) {
        this.webSocketProperties = webSocketProperties;
        this.handshakeAuthList = handshakeAuthList;
        this.handlerMessageService = handlerMessageService.orElse(null);
        this.webSocketHandlerListener = webSocketHandlerListener.orElse(null);
    }


    /**
     * 1. addInterceptors 添加拦截器
     * <p>
     * 2. setAllowedOrigins默认websocket只接受同源请求，配置跨域
     * <p>
     * 3. 不考虑兼容SocketJs
     *
     * @param registry Bean 定义注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WebSocketHandlerRegistration registration = registry.addHandler(
                        new DefaultWebSocketHandler(handlerMessageService, webSocketHandlerListener),
                        webSocketProperties.getEndPoint())
                .setHandshakeHandler(new CustomizeHandshakeHandler())
                .setAllowedOrigins("*");
        if (CollUtil.isNotEmpty(webSocketProperties.getHandshakeInterceptors())) {
            registration.addInterceptors(
                    webSocketProperties.getHandshakeInterceptors().toArray(new HandshakeInterceptor[0]));
        } else {
            registration.addInterceptors(new DefaultHandshakeInterceptor(webSocketProperties, handshakeAuthList));
        }
    }

    /**
     * 每个底层WebSocket引擎都公开控制运行时特征的配置属性，例如消息缓冲区大小，空闲超时等。
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置消息缓冲区大小(经测试可以控制消息通讯时的传输数据大小，如果超过大小，会关闭链接)
        // CloseStatus: CloseStatus[code=1009, reason=The decoded text message was too big for the output buffer and the endpoint does not support partial messages]
        container.setMaxTextMessageBufferSize(webSocketProperties.getMaxTextMessageBufferSize());
        container.setMaxBinaryMessageBufferSize(webSocketProperties.getMaxBinaryMessageBufferSize());
        // 如果是session的最大空闲时间，那么后面开发心跳包的时候这里就要让心跳包小于这个时间
        container.setMaxSessionIdleTimeout(webSocketProperties.getMaxSessionIdleTimeout());
        return container;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public RSAEncryptProcessor rsaEncryptProcessor() {
        return new RSAEncryptProcessor();
    }

    @Bean
    public CmdStrategyHelper cmdStrategyHelper(ThreadPoolTaskExecutor deviceCmdRunningStatePersistencePool) {
        return new CmdStrategyHelper(deviceCmdRunningStatePersistencePool);
    }

    @Bean
    public ChannelTransferService channelTransferService() {
        return new ChannelTransferServiceImpl();
    }

    @Bean
    public HandlerMessageService handlerMessageService(ThreadPoolTaskExecutor handlerMessagePool,
            ChannelTransferService channelTransferService, CmdStrategyHelper cmdStrategyHelper) {
        return new HandlerMessageServiceImpl(handlerMessagePool, channelTransferService, webSocketProperties,
                cmdStrategyHelper);
    }

    @Bean
    public WsMessageService wsMessageService(StringRedisTemplate stringRedisTemplate, Environment environment,
            ThreadPoolTaskExecutor batchCmdExecutor, ChannelTransferService channelTransferService,
            List<WsMessageFilter> wsMessageFilters) {
        return new WsMessageServiceImpl(Optional.of(channelTransferService), stringRedisTemplate, environment,
                batchCmdExecutor, wsMessageFilters);
    }

    @Bean
    public RedirectCmdListener redirectCmdListener(WsMessageService wsMessageService) {
        return new RedirectCmdListener(wsMessageService);
    }

    @Bean
    public RemoveOfflineKeyListener removeOfflineKeyListener(StringRedisTemplate stringRedisTemplate,
            Environment environment) {
        return new RemoveOfflineKeyListener(stringRedisTemplate, environment);
    }

    @Bean
    public ServerNodeOfflineListener serverNodeOfflineListener(RemoveOfflineKeyListener removeOfflineKeyListener) {
        return new ServerNodeOfflineListener(removeOfflineKeyListener);
    }
}
