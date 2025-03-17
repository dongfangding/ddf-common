package com.ddf.boot.common.rocketmq.producer;

import com.alibaba.fastjson.JSONObject;
import com.ddf.boot.common.rocketmq.config.RocketEnhanceProperties;
import com.ddf.boot.common.rocketmq.domain.MessagePayload;
import com.ddf.boot.common.rocketmq.domain.RocketMqMessage;
import com.google.common.base.Throwables;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class RocketProducer {

    private final RocketMQTemplate template;

    @Resource
    private RocketEnhanceProperties rocketEnhanceProperties;

    private final String TAG = "RocketProducerPlus:";

    public RocketMQTemplate getTemplate() {
        return template;
    }

    /**
     * 根据系统上下文自动构建隔离后的topic
     * 构建目的地
     */
    private String buildDestination(String topic, String tag) {
        topic = reBuildTopic(topic);
        return topic + ":" + tag;
    }

    /**
     * 根据环境重新隔离topic
     *
     * @param topic 原始topic
     */
    private String reBuildTopic(String topic) {
        if (rocketEnhanceProperties.isEnabledIsolation() && StringUtils.hasText(
                rocketEnhanceProperties.getEnvironment())) {
            return topic + "_" + rocketEnhanceProperties.getEnvironment();
        }
        return topic;
    }

    /**
     * 异步发送MQ消息
     */
    public <T> void asyncSend(RocketMqMessage rocketMqMessage) {
        rocketMqMessage.check();
        Long delayTime = rocketMqMessage.getDelayTime();
        if (delayTime != null) {
            throw new RuntimeException("延迟消息不支持异步发送");
        }

        String topic = rocketMqMessage.getTopic();
        String tag = rocketMqMessage.getExpression();
        MessagePayload message = rocketMqMessage.getPayLoad();
        try {
            log.debug("[{}] Sending message to MQ topic {}, context {}", TAG, topic, message);
            SendCallback callback = new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    // 打印msgId用来以备查验,外部消息发送mq成功则任务置为成功
                    log.info("[{}] Success sending message to TOPIC: {},TAG:{}, context: {}, msgId: {}", TAG, topic,tag, message,
                            sendResult.getMsgId()
                    );
                }

                @Override
                public void onException(Throwable e) {
                    log.error("[{}] Failed to send message to MQ {},TAG:{}, msg {}, cause {}", TAG, topic, tag,message,
                            Throwables.getStackTraceAsString(e)
                    );
                }
            };
            Message<MessagePayload> build = MessageBuilder.withPayload(message).setHeader(
                    RocketMQHeaders.KEYS, message.getMessageId()).build();
            template.asyncSend(buildDestination(topic, tag), build, callback);
        } catch (Exception e) {
            log.error("[{}] Failed to send message to MQ! TOPIC: {},TAG:{}, message: {}, stackTrace: {}", TAG, topic,tag,message,
                    Throwables.getStackTraceAsString(e)
            );
        }
    }


    /**
     * 发送同步消息
     */
    public SendResult syncSend(RocketMqMessage rocketMqMessage) {
        rocketMqMessage.check();
        String topic = rocketMqMessage.getTopic();
        String tag = rocketMqMessage.getExpression();
        MessagePayload payload = rocketMqMessage.getPayLoad();
        Long delayTime = rocketMqMessage.getDelayTime();
        //延迟消息
        if (delayTime != null && delayTime > 0){
            return sendDelay(buildDestination(topic, tag), payload, delayTime);
        }
        // 注意分隔符
        return send(buildDestination(topic, tag), payload);
    }

    /**同步消息**/
    private <T extends MessagePayload> SendResult send(String destination, T message) {
        // 设置业务键，此处根据公共的参数进行处理
        // 更多的其它基础业务处理...
        Message<T> sendMessage = MessageBuilder.withPayload(message).setHeader(
                RocketMQHeaders.KEYS, message.getMessageId()).build();
        SendResult sendResult = template.syncSend(destination, sendMessage);
        // 此处为了方便查看给日志转了json，根据选择选择日志记录方式，例如ELK采集
        log.info("[{}] [{}] 同步消息[{}]发送结果[{}]", TAG, destination, JSONObject.toJSON(message),
                JSONObject.toJSON(sendResult)
        );
        return sendResult;
    }

    /**延迟消息**/
    private <T extends MessagePayload> SendResult sendDelay(String destination, T message, Long delayTime) {
        Message<T> sendMessage = MessageBuilder.withPayload(message).setHeader(
                RocketMQHeaders.KEYS, message.getMessageId()).build();
        SendResult sendResult = template.syncSendDelayTimeSeconds(destination, sendMessage, delayTime);
        log.info("[{}] [{}]延迟时间 [{}s]消息[{}]发送结果[{}]", TAG, destination, delayTime,
                JSONObject.toJSON(message), JSONObject.toJSON(sendResult)
        );
        return sendResult;
    }
}
