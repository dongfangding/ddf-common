package com.ddf.boot.common.websocket.config;


import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.websocket.handler.CustomizeHandshakeHandler;
import com.ddf.boot.common.websocket.handler.DefaultWebSocketHandler;
import com.ddf.boot.common.websocket.handler.HandlerMessageService;
import com.ddf.boot.common.websocket.interceptor.DefaultHandshakeInterceptor;
import com.ddf.boot.common.websocket.interceptor.HandshakeAuth;
import com.ddf.boot.common.websocket.listeners.WebSocketHandlerListener;
import com.ddf.boot.common.websocket.properties.WebSocketProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.List;


/**
 * ws的配置类
 * 参考 https://docs.spring.io/spring/docs/5.1.9.RELEASE/spring-framework-reference/web.html#websocket
 *
 * 当前项目未定型stomp over websocket
 *
 *
 * @author dongfang.ding
 * @date 2019/8/20 11:43
 */
@Configuration
@EnableWebSocket
@MapperScan(basePackages = "com.ddf.boot.common.websocket.mapper")
@ComponentScan(basePackages = "com.ddf.boot.common.websocket")
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketProperties webSocketProperties;
    @Autowired(required = false)
    private List<HandshakeAuth> handshakeAuthList;
    @Autowired(required = false)
    private HandlerMessageService handlerMessageService;
    @Autowired(required = false)
    private WebSocketHandlerListener webSocketHandlerListener;


    /**
     * 1. addInterceptors 添加拦截器
     * <p>
     * 2. setAllowedOrigins默认websocket只接受同源请求，配置跨域
     * <p>
     * 3. 不考虑兼容SocketJs
     *
     * @param registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WebSocketHandlerRegistration registration = registry.addHandler(
                new DefaultWebSocketHandler(handlerMessageService, webSocketHandlerListener), webSocketProperties.getEndPoint())
                .setHandshakeHandler(new CustomizeHandshakeHandler())
                .setAllowedOrigins("*");
        if (CollUtil.isNotEmpty(webSocketProperties.getHandshakeInterceptors())) {
            registration.addInterceptors(webSocketProperties.getHandshakeInterceptors().toArray(new HandshakeInterceptor[0]));
        } else {
            registration.addInterceptors(new DefaultHandshakeInterceptor(webSocketProperties, handshakeAuthList));
        }
    }

    /**
     * 每个底层WebSocket引擎都公开控制运行时特征的配置属性，例如消息缓冲区大小，空闲超时等。
     *
     * @return
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
}