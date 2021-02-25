package com.ddf.boot.common.rocketmq.helper;

import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.rocketmq.dto.RocketMQDestination;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 * <p>
 * 消息发送样例
 * https://github.com/apache/rocketmq/blob/master/docs/cn/RocketMQ_Example.md
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/11/20 11:32
 */
@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Slf4j
public class RocketMQHelper {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 发送普通消息
     *
     * @param destination
     * @param msg
     */
    public void syncSend(final RocketMQDestination destination, final Object msg) {
        syncSend(destination, msg, null);
    }

    /**
     * 发送普通消息
     *
     * @param destination {@link RocketMQTemplate#syncSend(String, Object)}
     * @param msg
     * @param keys
     */
    public void syncSend(final RocketMQDestination destination, final Object msg, @Nullable String keys) {
        String destinationStr = destination.toDestination();
        if (Objects.isNull(keys)) {
            keys = destinationStr + "_" + IdsUtil.getNextStrId();
        }

        Message<?> message = MessageBuilder.withPayload(msg)
                // 埋入消息key，便于查询问题
                .setHeader(RocketMQHeaders.KEYS, keys).build();
        // https://github.com/apache/rocketmq/blob/master/docs/cn/best_practice.md#3-%E6%97%A5%E5%BF%97%E7%9A%84%E6%89%93%E5%8D%B0
        final SendResult sendResult = rocketMQTemplate.syncSend(destination.toDestination(), message);
        log.info(
                "send message to `{}` with keys[{}] finished. result:{}....message payload: {}",
                destination.toDestination(), keys, sendResult, JsonUtil.asString(msg)
        );
    }

}
