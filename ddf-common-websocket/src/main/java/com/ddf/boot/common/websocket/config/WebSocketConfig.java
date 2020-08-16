package com.ddf.boot.common.websocket.config;


import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.interceptor.CustomizeHandshakeInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


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
@MapperScan(basePackages = {"com.ddf.boot.common.websocket.mapper"})
@ComponentScan(basePackages = "com.ddf.boot.common.websocket")
public class WebSocketConfig implements WebSocketConfigurer {


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
        registry.addHandler(new CustomizeWebSocketHandler(), WebsocketConst.DEFAULT_ENDPOINT)
                .setHandshakeHandler(new CustomizeHandshakeHandler())
                .addInterceptors(new CustomizeHandshakeInterceptor())
                .setAllowedOrigins("*");
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
        container.setMaxTextMessageBufferSize(8192 * 2);
        container.setMaxBinaryMessageBufferSize(8192 * 2);
        // 如果是session的最大空闲时间，那么后面开发心跳包的时候这里就要让心跳包小于这个时间
        container.setMaxSessionIdleTimeout(60000L);
        return container;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}