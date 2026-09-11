package com.ddf.boot.common.stomp.helpere;

import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.redis.ext.RedisTopic;
import com.ddf.boot.common.redis.request.RedisBroadcastMsg;
import com.ddf.boot.common.stomp.model.dto.StompMessageProtocol;
import com.ddf.boot.common.stomp.model.req.StompMessageRequest;
import java.net.Inet4Address;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * STOMP消息发送帮助类，支持WebSocket推送和基于Redis的跨实例广播
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/09/08 12:08
 */
@Slf4j
public class StompMessageHelper {

    private final SimpMessagingTemplate messagingTemplate;

    private final RedissonClient redissonClient;

    private RedisTopic redisTopic;

    public StompMessageHelper(SimpMessagingTemplate messagingTemplate, RedissonClient redissonClient) {
        this.messagingTemplate = messagingTemplate;
        this.redissonClient = redissonClient;
        redisTopic = RedisTopic.newInstance("stomp:broadcast", redissonClient);
        redisTopic.addListener(RedisBroadcastMsg.class, (channel, message) -> broadcastMsg(channel.toString(), message));
    }

    /**
     * 发送消息：通过WebSocket推送到本实例连接的客户端，同时通过Redis Pub/Sub广播到其他实例
     *
     * @param request 消息请求
     */
    public <T> void sendMessage(StompMessageRequest<T> request) {
        push(request);
        // 基于Redis Pub/Sub广播到其他实例
        broadcastViaRedis(request.getTopic(), JsonUtil.toJson(request));
    }


    private <T> void push(StompMessageRequest<T> request) {
        final String payload = buildPayload(request);
        // WebSocket推送到本实例连接的客户端
        messagingTemplate.convertAndSend(request.getTopic(), payload);
    }

    /**
     * 构建STOMP消息协议体
     */
    private <T> String buildPayload(StompMessageRequest<T> request) {
        final StompMessageProtocol protocol = new StompMessageProtocol();
        protocol.setTitle(request.getTitle());
        protocol.setMessageCode(request.getMessageCode());
        protocol.setData(JsonUtil.toJson(request.getData()));
        return JsonUtil.toJson(protocol);
    }

    /**
     * 通过Redis Pub/Sub广播消息，用于多实例环境下将消息同步到其他节点的WebSocket连接
     */
    private void broadcastViaRedis(String topic, String payload) {
        if (redissonClient == null) {
            return;
        }
        try {
            final RedisBroadcastMsg msg = new RedisBroadcastMsg();
            msg.setDelegateHost(Inet4Address.getLocalHost().getHostAddress());
            msg.setExcludeDelegate(true);
            msg.setMsg(payload);
            redisTopic.publish(msg);
        } catch (Exception e) {
            log.error("Redis broadcast failed for topic [{}]", topic, e);
        }
    }

    private <T> void broadcastMsg(CharSequence channel, RedisBroadcastMsg requestMsg) {
        try {
            log.error("Redis broadcast failed for topic [{}]， msg： {}", channel, requestMsg);
            final String host = requestMsg.getDelegateHost();
            final boolean isExcludeDelegate = requestMsg.isExcludeDelegate();
            if (!isExcludeDelegate || !host.equals(Inet4Address.getLocalHost().getHostAddress())) {
                push(JsonUtil.toBean(requestMsg.getMsg(), StompMessageRequest.class));
            }
        } catch (Exception e) {
            log.error("Redis broadcast failed for topic [{}]", channel, e);
        }
    }
}
