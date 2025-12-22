package com.ddf.boot.common.rocketmq.handler;

import com.alibaba.fastjson2.JSONObject;
import com.ddf.boot.common.rocketmq.constant.EnhanceMessageConstant;
import com.ddf.boot.common.rocketmq.domain.MessagePayload;
import com.ddf.boot.common.rocketmq.domain.RocketMqMessage;
import com.ddf.boot.common.rocketmq.producer.RocketProducer;
import com.google.common.base.Throwables;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

@Slf4j
public abstract class EnhanceMessageHandler<T> implements RocketMQListener<MessagePayload<T>> {

    private static final String TAG = "消费者处理器";

    /**
     * 默认重试次数
     */
    private static final int MAX_RETRY_TIMES = 3;

    /**
     * 延时等级
     */
    private static final Long DELAY_LEVEL = EnhanceMessageConstant.FIVE_SECOND;


    @Resource
    private RocketProducer rocketProducer;

    /**
     * 消息处理
     *
     * @param message 待处理消息
     * @throws Exception 消费异常
     */
    protected abstract void handleMessage(T message) throws Exception;

    /**
     * 超过重试次数消息，需要启用isRetry
     *
     * @param message 待处理消息
     */
    protected void handleMaxRetriesExceeded(MessagePayload message){
        log.error("消息Id:{},达到最大重试次数，消费失败，请执行后续处理",message.getMessageId());
    }


    /**
     * 是否需要根据业务规则过滤消息，去重逻辑可以在此处处理
     *
     * @param message 待处理消息
     * @return true: 本次消息被过滤，false：不过滤
     */
    protected boolean filter(MessagePayload message) {
        return false;
    }

    /**
     * 是否异常时重复发送
     *
     * @return true: 消息重试，false：不重试
     */
    protected boolean isRetry() {
        return false;
    }

    /**
     * 消费异常时是否抛出异常
     * 返回true，则由rocketmq机制自动重试
     * false：消费异常(如果没有开启重试则消息会被自动ack)
     */
    abstract protected boolean throwException();

    /**
     * 最大重试次数
     *
     * @return 最大重试次数，默认5次
     */
    protected int getMaxRetryTimes() {
        return MAX_RETRY_TIMES;
    }

    /**
     * isRetry开启时，重新入队延迟时间
     *
     * @return -1：立即入队重试
     */
    protected Long getDelaySeconds() {
        return DELAY_LEVEL;
    }

    @Override
    public void onMessage(MessagePayload payload) {
        dispatchMessage(payload);
    }

    private Class<T> clazz;

    public EnhanceMessageHandler() {
        // Type genericSuperclass = getClass().getGenericSuperclass();
        // Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        // clazz = (Class) ((ParameterizedType) actualTypeArguments[0]).getActualTypeArguments()[0];
        // System.out.println();

        Type genericSuperclass = getClass().getGenericSuperclass();
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        clazz = (Class<T>) actualTypeArguments[0];
    }

    public Class<T> getMsgOriginalClazzType() {
        return clazz;
    }

    /**
     * 使用模板模式构建消息消费框架，可自由扩展或删减
     */
    public void dispatchMessage(MessagePayload<T> message) {
        // 基础日志记录被父类处理了
        String messageJson = JSONObject.toJSONString(message);
        log.info("[{}] 消费者收到消息[{}]", TAG, messageJson);
        if (filter(message)) {
            log.info("[{}] 消息id{}不满足消费条件,中断消费...", TAG, message.getMessageId());
            return;
        }
        // 超过最大重试次数时调用子类方法处理
        if (message.getRetryTimes() > getMaxRetryTimes()) {
            handleMaxRetriesExceeded(message);
            return;
        }
        try {
            long now = System.currentTimeMillis();
            T data = message.getData();
            handleMessage(data);
            long costTime = System.currentTimeMillis() - now;
            log.info("[{}] 消息id:{}消费成功,messageData:{},耗时[{}ms]", TAG,message.getMessageId(),messageJson, costTime);
        } catch (Exception e) {
            log.info("[{}] 消息id:{}消费异常,e:{}",TAG, message.getMessageId(), Throwables.getStackTraceAsString(e));
            // 是捕获异常还是抛出，由子类决定
            if (throwException()) {
                // 抛出异常，由DefaultMessageListenerConcurrently类处理
                throw new RuntimeException(e);
            }
            // 此时如果不开启重试机制，则默认ACK了
            if (isRetry()) {
                handleRetry(message);
            }
        }
    }

    /**
     * 处理重试
     *
     * @param message 信息
     */
    protected void handleRetry(MessagePayload message) {
        // 获取子类RocketMQMessageListener注解拿到topic和tag
        RocketMQMessageListener annotation = this.getClass().getAnnotation(RocketMQMessageListener.class);
        if (annotation == null) {
            return;
        }
        // 重新构建消息体
        String messageSource = message.getSource();
        if (!messageSource.startsWith(EnhanceMessageConstant.RETRY_PREFIX)) {
            message.setSource(EnhanceMessageConstant.RETRY_PREFIX + messageSource);
        }
        message.setRetryTimes(message.getRetryTimes() + 1);

        SendResult sendResult = null;
        try {
            // 如果消息发送不成功，则再次重新发送，如果发送异常则抛出由MQ再次处理(异常时不走延迟消息)
            RocketMqMessage delayRocketMqMessage = new RocketMqMessage();
            delayRocketMqMessage.setTopic(annotation.topic());
            delayRocketMqMessage.setExpression(annotation.selectorExpression());
            delayRocketMqMessage.setPayLoad(message);
            delayRocketMqMessage.setDelayTime(getDelaySeconds());
            sendResult = rocketProducer.syncSend(delayRocketMqMessage);
        } catch (Exception ex) {
            log.error("[{}] 消息id:{},发送重试消息异常,e:{}",TAG, message.getMessageId(), Throwables.getStackTraceAsString(ex));
        }
        // 发送失败的处理就是不进行ACK，由RocketMQ重试
        if (Objects.isNull(sendResult) || sendResult.getSendStatus() != SendStatus.SEND_OK) {
            log.error("[{}] 消息id:{},发送重试消息异常,sendStatus:{}",TAG, message.getMessageId(), sendResult.getSendStatus());
        }

    }
}
