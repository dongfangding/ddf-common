package com.ddf.boot.common.redis.ext;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

/**
 * 基于Redis实现topic订阅与发布{@link RTopic}
 *
 * @author Mitchell
 * @version 1.0
 * @date 2020/09/18 13:42
 *
 * <pre>
 *     @Test
 *     public void test() {
 *         RedisTopic<Message> topic = RedisTopic.newInstance("test-topic", redissonClient);
 *         topic.addListener((channel, msg) -> log.info("this [one] consumer {} {}", channel, msg));
 *         topic.addListener((channel, msg) -> log.info("this [two] consumer {} {}", channel, msg));
 *         topic.publish(new Message().setContent("hello word!~~~"));
 *         ThreadUtil.sleep(1000000);
 *     }
 * </pre>
 */
@Slf4j
public class RedisTopic {

    private final String name;

    private final RTopic topic;

    public static RedisTopic newInstance(String name, RedissonClient redissonClient) {
        RTopic topic = redissonClient.getTopic(name);
        return new RedisTopic(name, topic);
    }

    private RedisTopic(String name, RTopic topic) {
        this.name = name;
        this.topic = topic;
    }

    /**
     * Publish the message to all subscribers of this topic
     *
     * @param message to send
     * @return the number of clients that received the message
     */
    public long publish(Object message) {
        long result = topic.publish(message);
        log.info("{}[{}] publish object={}, result={}", this.getClass().getSimpleName(), name, message, result);
        return result;
    }

    /**
     * Publish the message to all subscribers of this topic asynchronously
     *
     * @param message to send
     * @return the <code>RFuture</code> object with number of clients that received the message
     */
    public RFuture<Long> publishAsync(Object message) {
        RFuture<Long> result = topic.publishAsync(message);
        log.info("{}[{}] publishAsync object={}, result={}", this.getClass().getSimpleName(), name, message, result);
        return result;
    }

    /**
     * Subscribes to this topic.
     * <code>MessageListener.onMessage</code> is called when any message
     * is published on this topic.
     *
     * @param listener for messages
     * @return locally unique listener id
     * @see org.redisson.api.listener.MessageListener
     */
    public <T> int addListener(Class<T> type, MessageListener<? extends T> listener) {
        int result = topic.addListener(type, listener);
        log.info("{}[{}] addListener object={}, result={}", this.getClass().getSimpleName(), name, listener, result);
        return result;
    }

}
