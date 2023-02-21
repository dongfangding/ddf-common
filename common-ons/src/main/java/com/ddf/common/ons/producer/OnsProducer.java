package com.ddf.common.ons.producer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.common.ons.mongodb.OnsMessageLogger;
import com.ddf.common.ons.properties.OnsProperties;
import com.ddf.common.ons.redis.OnsRedisService;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ONS消息生产者
 *
 * @author snowball
 * @date 2021/8/26 15:02
 **/
public abstract class OnsProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger("OnsProducer");
    private static final Logger LOGGER_ERROR = LoggerFactory.getLogger("OnsProducerError");
    private static final OrderProducerBean ORDER_PRODUCER;
    private static final ProducerBean PRODUCER;
    private static final OnsProperties ONS_PROPERTIES;
    private static final String PRODUCE_RETRY_TIMES_PREFIX = "produce_retry_times:";

    static {
        ORDER_PRODUCER = SpringContextHolder.getBean("orderProducer", OrderProducerBean.class);
        PRODUCER = SpringContextHolder.getBean("producer", ProducerBean.class);
        ONS_PROPERTIES= SpringContextHolder.getBean(OnsProperties.class);
    }

    /**
     * 同步发送MQ顺序消息
     *
     * @param onsMessage
     */
    public static void orderSend(OnsMessage onsMessage) {
        onsMessage.checkOrder();
        String topic = onsMessage.getTopic();
        String tag = onsMessage.getExpression();
        String payLoad = onsMessage.getPayLoad();
        String bizId = onsMessage.getWrapperBizId();
        String shadingKey = onsMessage.getShadingKey();
        Long delayTime = onsMessage.getDelayTime();

        Message message = new Message(topic, tag, payLoad.getBytes());
        message.setKey(bizId);
        if (Objects.nonNull(delayTime) && delayTime > 0) {
            // 延时消息，单位毫秒（ms），在指定延迟时间（当前时间之后）进行投递
            long startDeliverTime = System.currentTimeMillis() + delayTime;
            message.setStartDeliverTime(startDeliverTime);
        }
        try {
            // 同步发送消息，只要不抛异常就是成功。
            SendResult sendResult = ORDER_PRODUCER.send(message, shadingKey);
            if (null != sendResult) {
                LOGGER.info("同步发送Topic:{}, Tag:{}, PayLoad:{}, Key:{}, ShadingKey:{}, DelayTime:{}顺序消息成功，MessageId:{}",
                        topic, tag, payLoad, bizId, shadingKey, delayTime, sendResult.getMessageId()
                );
                OnsMessageLogger.infoProduceMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER_ERROR.error("同步发送Topic:{},Tag:{},PayLoad:{},Key:{},ShadingKey:{},DelayTime:{}顺序消息失败:", topic, tag,
                    payLoad, bizId, shadingKey, delayTime, e
            );
            OnsMessageLogger.errorProduceMessage(message, e.getMessage());
            if (isRetryable(bizId)) {
                incrementRetryTimes(bizId);
                orderSend(topic, tag, payLoad, bizId, shadingKey, delayTime);
            }
        }
    }

    /**
     * 同步发送MQ顺序消息
     *
     * @param topic      主题
     * @param tag        标签
     * @param payLoad    消息体
     * @param bizId      业务Id，每次发送必须唯一
     * @param shadingKey 分区顺序消息中区分不同分区的关键字段，Sharding Key 与普通消息的 key 是完全不同的概念。
     *                   全局顺序消息，该字段可以设置为任意非空字符串。
     * @see OnsProducer#orderSend(OnsMessage)
     */
    @Deprecated
    public static void orderSend(String topic, String tag, String payLoad, String bizId, String shadingKey) {
        orderSend(topic, tag, payLoad, bizId, shadingKey, null);
    }

    /**
     * 同步发送MQ顺序消息
     *
     * @param topic      主题
     * @param tag        标签
     * @param payLoad    消息体
     * @param bizId      业务Id，每次发送必须唯一
     * @param shadingKey 分区顺序消息中区分不同分区的关键字段，Sharding Key 与普通消息的 key 是完全不同的概念。
     *                   全局顺序消息，该字段可以设置为任意非空字符串。
     * @param delayTime  发送延时消息的延时时间，单位毫秒
     * @see OnsProducer#orderSend(OnsMessage)
     */
    @Deprecated
    public static void orderSend(String topic, String tag, String payLoad, String bizId, String shadingKey,
            Long delayTime) {
        try {
            Message message = new Message(topic, tag, payLoad.getBytes());
            message.setKey(bizId);
            if (Objects.nonNull(delayTime) && delayTime > 0) {
                // 延时消息，单位毫秒（ms），在指定延迟时间（当前时间之后）进行投递
                long startDeliverTime = System.currentTimeMillis() + delayTime;
                message.setStartDeliverTime(startDeliverTime);
            }
            // 同步发送消息，只要不抛异常就是成功。
            SendResult sendResult = ORDER_PRODUCER.send(message, shadingKey);
            if (null != sendResult) {
                LOGGER.info("同步发送Topic:{},Tag:{},PayLoad:{},Key:{},ShadingKey:{},DelayTime:{}顺序消息成功，MessageId:{}",
                        topic, tag, payLoad, bizId, shadingKey, delayTime, sendResult.getMessageId()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER_ERROR.error("同步发送Topic:{},Tag:{},PayLoad:{},Key:{},ShadingKey:{},DelayTime:{}顺序消息失败:", topic, tag,
                    payLoad, bizId, shadingKey, delayTime, e
            );
            if (isRetryable(bizId)) {
                incrementRetryTimes(bizId);
                orderSend(topic, tag, payLoad, bizId, shadingKey, delayTime);
            }
        }
    }

    /**
     * 同步发送MQ消息
     *
     * @param onsMessage
     */
    public static void send(OnsMessage onsMessage) {
        onsMessage.check();
        String topic = onsMessage.getTopic();
        String tag = onsMessage.getExpression();
        String payLoad = onsMessage.getPayLoad();
        String bizId = onsMessage.getWrapperBizId();
        Long delayTime = onsMessage.getDelayTime();

        
        Message message = new Message(topic, tag, payLoad.getBytes());
        message.setKey(bizId);
        if (Objects.nonNull(delayTime) && delayTime > 0) {
            // 延时消息，单位毫秒（ms），在指定延迟时间（当前时间之后）进行投递
            long startDeliverTime = System.currentTimeMillis() + delayTime;
            message.setStartDeliverTime(startDeliverTime);
        }
        try {
            // 同步发送消息，只要不抛异常就是成功。
            SendResult sendResult = PRODUCER.send(message);
            if (null != sendResult) {
                LOGGER.info("同步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息成功，MessageId:{}", topic, tag, payLoad,
                        bizId, delayTime, sendResult.getMessageId()
                );
                OnsMessageLogger.infoProduceMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER_ERROR.error("同步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息失败:", topic, tag, payLoad, bizId,
                    delayTime, e
            );
            OnsMessageLogger.errorProduceMessage(message, e.getMessage());
            if (isRetryable(bizId)) {
                incrementRetryTimes(bizId);
                send(topic, tag, payLoad, bizId, delayTime);
            }
        }
    }

    /**
     * 同步发送MQ消息
     *
     * @param topic   主题
     * @param tag     标签
     * @param payLoad 消息体
     * @param bizId   业务Id，每次发送必须唯一
     * @see OnsProducer#send(OnsMessage)
     */
    @Deprecated
    public static void send(String topic, String tag, String payLoad, String bizId) {
        send(topic, tag, payLoad, bizId, null);
    }

    /**
     * 同步发送MQ消息
     *
     * @param topic     主题
     * @param tag       标签
     * @param payLoad   消息体
     * @param bizId     业务Id，每次发送必须唯一
     * @param delayTime 发送延时消息的延时时间，单位毫秒
     * @see OnsProducer#send(OnsMessage)
     */
    @Deprecated
    public static void send(String topic, String tag, String payLoad, String bizId, Long delayTime) {
        try {
            Message message = new Message(topic, tag, payLoad.getBytes());
            message.setKey(bizId);
            if (Objects.nonNull(delayTime) && delayTime > 0) {
                // 延时消息，单位毫秒（ms），在指定延迟时间（当前时间之后）进行投递
                long startDeliverTime = System.currentTimeMillis() + delayTime;
                message.setStartDeliverTime(startDeliverTime);
            }
            // 同步发送消息，只要不抛异常就是成功。
            SendResult sendResult = PRODUCER.send(message);
            if (null != sendResult) {
                LOGGER.info("同步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息成功，MessageId:{}", topic, tag, payLoad,
                        bizId, delayTime, sendResult.getMessageId()
                );
                //removeRetryTimes(bizId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER_ERROR.error("同步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息失败:", topic, tag, payLoad, bizId,
                    delayTime, e
            );
            if (isRetryable(bizId)) {
                incrementRetryTimes(bizId);
                send(topic, tag, payLoad, bizId, delayTime);
            }
        }
    }

    /**
     * 异步发送MQ消息
     *
     * @param onsMessage
     */
    public static void sendAsync(OnsMessage onsMessage) {
        onsMessage.check();
        String topic = onsMessage.getTopic();
        String tag = onsMessage.getExpression();
        String payLoad = onsMessage.getPayLoad();
        String bizId = onsMessage.getBizId();
        Long delayTime = onsMessage.getDelayTime();

        
        Message message = new Message(topic, tag, payLoad.getBytes());
        message.setKey(bizId);
        if (Objects.nonNull(delayTime) && delayTime > 0) {
            // 延时消息，单位毫秒（ms），在指定延迟时间（当前时间之后）进行投递
            long startDeliverTime = System.currentTimeMillis() + delayTime;
            message.setStartDeliverTime(startDeliverTime);
        }
        try {
            // 异步发送消息, 发送结果通过 callback 返回给客户端。
            PRODUCER.sendAsync(message, new SendCallback() {
                @Override
                public void onSuccess(final SendResult sendResult) {
                    LOGGER.info("异步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息成功，MessageId:{}", topic, tag,
                            payLoad, bizId, delayTime, sendResult.getMessageId()
                    );
                    OnsMessageLogger.infoProduceMessage(message);
                }

                @Override
                public void onException(OnExceptionContext context) {
                    String errorMessage = context.getException()
                            .getMessage();
                    LOGGER_ERROR.error("异步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息失败:{}，MessageId:{}", topic,
                            tag, payLoad, bizId, delayTime, errorMessage, context.getMessageId()
                    );
                    OnsMessageLogger.errorProduceMessage(message, errorMessage);
                    if (isRetryable(bizId)) {
                        incrementRetryTimes(bizId);
                        sendAsync(topic, tag, payLoad, bizId, delayTime);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER_ERROR.error("异步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息失败:", topic, tag, payLoad, bizId,
                    delayTime, e
            );
            OnsMessageLogger.errorProduceMessage(message, e.getMessage());
            if (isRetryable(bizId)) {
                incrementRetryTimes(bizId);
                sendAsync(topic, tag, payLoad, bizId, delayTime);
            }
        }
    }

    /**
     * 异步发送MQ消息
     *
     * @param topic   主题
     * @param tag     标签
     * @param payLoad 消息体
     * @param bizId   业务Id，每次发送必须唯一
     * @see OnsProducer#sendAsync(OnsMessage)
     */
    @Deprecated
    public static void sendAsync(String topic, String tag, String payLoad, String bizId) {
        sendAsync(topic, tag, payLoad, bizId, null);
    }

    /**
     * 异步发送MQ消息
     *
     * @param topic     主题
     * @param tag       标签
     * @param payLoad   消息体
     * @param bizId     业务Id，每次发送必须唯一
     * @param delayTime 发送延时消息的延时时间，单位毫秒
     * @see OnsProducer#sendAsync(OnsMessage)
     */
    @Deprecated
    public static void sendAsync(String topic, String tag, String payLoad, String bizId, Long delayTime) {
        try {
            Message message = new Message(topic, tag, payLoad.getBytes());
            message.setKey(bizId);
            if (Objects.nonNull(delayTime) && delayTime > 0) {
                // 延时消息，单位毫秒（ms），在指定延迟时间（当前时间之后）进行投递
                long startDeliverTime = System.currentTimeMillis() + delayTime;
                message.setStartDeliverTime(startDeliverTime);
            }
            // 异步发送消息, 发送结果通过 callback 返回给客户端。
            PRODUCER.sendAsync(message, new SendCallback() {
                @Override
                public void onSuccess(final SendResult sendResult) {
                    LOGGER.info("异步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息成功，MessageId:{}", topic, tag,
                            payLoad, bizId, delayTime, sendResult.getMessageId()
                    );
                }

                @Override
                public void onException(OnExceptionContext context) {
                    LOGGER_ERROR.error("异步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息失败:{}，MessageId:{}", topic,
                            tag, payLoad, bizId, delayTime, context.getException()
                                    .getMessage(), context.getMessageId()
                    );
                    if (isRetryable(bizId)) {
                        incrementRetryTimes(bizId);
                        sendAsync(topic, tag, payLoad, bizId, delayTime);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER_ERROR.error("异步发送Topic:{},Tag:{},PayLoad:{},Key:{},DelayTime:{}消息失败:", topic, tag, payLoad, bizId,
                    delayTime, e
            );
            if (isRetryable(bizId)) {
                incrementRetryTimes(bizId);
                sendAsync(topic, tag, payLoad, bizId, delayTime);
            }
        }
    }

    /**
     * 增加重试次数
     *
     * @param bizId
     * @return
     */
    public static long incrementRetryTimes(String bizId) {
        String key = PRODUCE_RETRY_TIMES_PREFIX + bizId;
        OnsRedisService onsRedisService = SpringContextHolder.getBean(OnsRedisService.BEAN_NAME, OnsRedisService.class);
        Long value = onsRedisService.incrAndExpire(key, 360);
        return value.longValue();
    }

    /**
     * 是否可以重试
     *
     * @param bizId
     * @return
     */
    public static boolean isRetryable(String bizId) {
        if (ONS_PROPERTIES.getProducer().isRetryEnabled()) {
            String key = PRODUCE_RETRY_TIMES_PREFIX + bizId;
            OnsRedisService onsRedisService = SpringContextHolder.getBean(
                    OnsRedisService.BEAN_NAME, OnsRedisService.class);
            Object value = onsRedisService.get(key);
            long produceRetryTimes = Objects.nonNull(value) ? Long.parseLong(value.toString()) : 0L;
            return produceRetryTimes < ONS_PROPERTIES.getProducer().getRetryTimes();
        }
        return false;
    }

    /**
     * 删除重试次数
     *
     * @param bizId
     */
    public static void removeRetryTimes(String bizId) {
        String key = PRODUCE_RETRY_TIMES_PREFIX + bizId;
        OnsRedisService onsRedisService = SpringContextHolder.getBean(OnsRedisService.BEAN_NAME, OnsRedisService.class);
        onsRedisService.delete(key);
    }

}
