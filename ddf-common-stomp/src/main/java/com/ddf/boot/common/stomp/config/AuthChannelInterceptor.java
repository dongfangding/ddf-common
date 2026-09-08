package com.ddf.boot.common.stomp.config;

import com.ddf.boot.common.stomp.repository.ImTokenRepository;
import jodd.util.StringUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * 认证拦截
 * stomp over websocket
 *
 * @author dongfang.ding
 * @since 2019/8/20 11:43
 */
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final ImTokenRepository imTokenRepository;

    public AuthChannelInterceptor(ImTokenRepository repository) {
        imTokenRepository = repository;
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("im_token");
            if (StringUtil.isBlank(token)) {
                throw new IllegalArgumentException("Token is missing");
            }
            // 自定义校验逻辑，例如数据库或 Redis 校验
            String userId = imTokenRepository.imTokenIsMatched(token);
            if (StringUtil.isBlank(userId)) {
                throw new IllegalArgumentException("Invalid token");
            }
            // 设置用户信息到 Principal
            accessor.setUser(userId::toString);
        }
        return message;
    }
}
