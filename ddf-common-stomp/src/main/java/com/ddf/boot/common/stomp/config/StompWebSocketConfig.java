package com.ddf.boot.common.stomp.config;


import com.ddf.boot.common.redis.helper.RedisCommandHelper;
import com.ddf.boot.common.stomp.helpere.StompMessageHelper;
import com.ddf.boot.common.stomp.repository.ImTokenRepository;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


/**
 * ws的配置类
 * stomp over websocket
 *
 * @author dongfang.ding
 * @since 2019/8/20 11:43
 */
@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 创建 TaskScheduler Bean
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        // 线程池大小，可以根据需要调整
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("ws-heartbeat-thread-");
        return taskScheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个STOMP端点，可以通过这个端点连接到WebSocket, 不支持socketjs
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }


    /**
     * 注册认证拦截器到客户端入站通道
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(RedisCommandHelper.class)
    static class RedisAuthChannelConfiguration implements WebSocketMessageBrokerConfigurer {

        private final ObjectProvider<AuthChannelInterceptor> authChannelInterceptorProvider;

        RedisAuthChannelConfiguration(ObjectProvider<AuthChannelInterceptor> authChannelInterceptorProvider) {
            this.authChannelInterceptorProvider = authChannelInterceptorProvider;
        }

        @Bean
        public ImTokenRepository imTokenRepository(RedisCommandHelper redisCommandHelper) {
            return new ImTokenRepository(redisCommandHelper);
        }

        @Bean
        public AuthChannelInterceptor authChannelInterceptor(ImTokenRepository imTokenRepository) {
            return new AuthChannelInterceptor(imTokenRepository);
        }

        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            registration.interceptors(authChannelInterceptorProvider.getObject());
        }
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user")
                // 前面是服务端心跳间隔，后面是客户端心跳间隔，当三个时间间隔没收到心跳，则认为连接断开，当然还受到container.setMaxSessionIdleTimeout(60000L);最大值影响
                .setHeartbeatValue(new long[] {60000, 30000})
                // 设置服务器心跳间隔：每 10000 毫秒发送一次心跳
                .setTaskScheduler(taskScheduler());
        // 应用前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 默认就是 /user，可省略
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 每个底层WebSocket引擎都公开控制运行时特征的配置属性，例如消息缓冲区大小，空闲超时等。
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置消息缓冲区大小(经测试可以控制消息通讯时的传输数据大小，如果超过大小，会关闭链接)
        // CloseStatus: CloseStatus[code=1009, reason=The decoded text message was too big for the output buffer and the endpoint does not support partial messages]
        // 每个连接都会分配这个缓冲区大小， 太大了活跃的连接会占用大量的内存
        container.setMaxTextMessageBufferSize(8 * 1000);
        container.setMaxBinaryMessageBufferSize(8 * 1000);
        // 如果是session的最大空闲时间，那么后面开发心跳包的时候这里就要让心跳包小于这个时间
        container.setMaxSessionIdleTimeout(60000L);
        return container;
    }

    /**
     * 消息发送
     *
     * @param messagingTemplate STOMP消息模板
     * @param redissonClientProvider Redisson客户端（可选，用于跨实例广播）
     */
    @Bean
    public StompMessageHelper stompMessageHelper(SimpMessagingTemplate messagingTemplate,
                                                  ObjectProvider<RedissonClient> redissonClientProvider) {
        return new StompMessageHelper(messagingTemplate, redissonClientProvider.getIfAvailable());
    }
}
