package com.ddf.common.ons.mongodb;

import com.aliyun.openservices.ons.api.Message;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.common.ons.properties.OnsProperties;
import java.net.InetAddress;
import java.util.Objects;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ONS消息日志器
 *
 * @author snowball
 * @date 2021/8/26 15:28
 **/
public abstract class OnsMessageLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnsMessageLogger.class);

    /**
     * 记录发送成功的消息日志
     * @param message
     */
    public static void infoProduceMessage(Message message){
        logProduceMessage(message, MessageStatusEnum.SUCCESS, null);
    }

    /**
     * 记录发送失败的消息日志
     * @param failureReason
     */
    public static void errorProduceMessage(Message message, String failureReason) {
        logProduceMessage(message, MessageStatusEnum.FAILURE, failureReason);
    }

    /**
     * 记录发送消息日志
     * @param messageStatus
     * @param failureReason
     */
    public static void logProduceMessage(Message message, MessageStatusEnum messageStatus, String failureReason) {
        try {
            OnsProperties onsConfiguration = SpringContextHolder.getBean(OnsProperties.class);
            OnsProperties.Producer producerProps = onsConfiguration.getProducer();
            final boolean isProduceSuccess = Objects.equals(MessageStatusEnum.SUCCESS, messageStatus);
            boolean isLogEnabled = isProduceSuccess ? producerProps.isLogSuccessfulEnabled() : producerProps.isLogFailureEnabled();
            if (isLogEnabled || isProduceSuccess) {
                OnsMessageLogService onsMessageLogService = SpringContextHolder.getBean(OnsMessageLogService.BEAN_NAME, OnsMessageLogService.class);
                OnsProduceMessageLog produceMessageLog = OnsProduceMessageLog.builder()
                        .topic(message.getTopic())
                        .expression(message.getTag())
                        .payLoad(new String(message.getBody()))
                        .bizId(message.getKey())
                        .shadingKey(message.getShardingKey())
                        .messageId(message.getMsgID())
                        .bornHost(getLocalHost())
                        .bornTimestamp(System.currentTimeMillis())
                        .startDeliverTime(message.getStartDeliverTime())
                        .partition(message.getTopicPartition().getPartition())
                        .offset(message.getOffset())
                        .messageStatus(messageStatus)
                        .failureReason(failureReason)
                        .build();

                boolean isRetry = false;
                if (isProduceSuccess) {
                    isRetry = onsMessageLogService.removeProduceFailureMessageLog(produceMessageLog).getDeletedCount() > 0;
                }

                if (isLogEnabled || isRetry) {
                    onsMessageLogService.upsertProduceMessageLog(produceMessageLog);
                }
            }
        }catch (Exception e) {
            LOGGER.error("记录发送消息日志失败：", e);
        }
    }

    /**
     * 记录消费成功的消息日志
     * @param message
     */
    public static void infoConsumeMessage(Message message,
                                          String consumer,
                                          String groupId){
        logConsumeMessage(message, MessageStatusEnum.SUCCESS, null, consumer, groupId);
    }

    /**
     * 记录消费失败的消息日志
     * @param failureReason
     */
    public static void errorConsumeMessage(Message message,
                                           String failureReason,
                                           String consumer,
                                           String groupId) {
        logConsumeMessage(message, MessageStatusEnum.FAILURE, failureReason, consumer, groupId);
    }


    /**
     * 记录消费消息日志
     * @param message
     * @param messageStatus
     * @param failureReason
     * @param consumer
     * @param groupId
     */
    public static void logConsumeMessage(Message message, MessageStatusEnum messageStatus, String failureReason,
                                         String consumer, String groupId) {
        try {
            OnsProperties onsConfiguration = SpringContextHolder.getBean(OnsProperties.class);
            OnsProperties.Consumer consumerProps = onsConfiguration.getConsumer();
            final boolean isConsumeSuccess = Objects.equals(MessageStatusEnum.SUCCESS, messageStatus);
            boolean isLogEnabled = isConsumeSuccess ?
                    consumerProps.isLogSuccessfulEnabled() : consumerProps.isLogFailureEnabled();
            if (isLogEnabled || isConsumeSuccess) {
                OnsMessageLogService onsMessageLogService = SpringContextHolder.getBean(OnsMessageLogService.BEAN_NAME, OnsMessageLogService.class);
                OnsConsumeMessageLog consumeMessageLog = new OnsConsumeMessageLog();

                consumeMessageLog.setTopic(message.getTopic());
                consumeMessageLog.setExpression(message.getTag());
                consumeMessageLog.setPayLoad(new String(message.getBody()));
                consumeMessageLog.setBizId(message.getKey());
                consumeMessageLog.setShadingKey(message.getShardingKey());
                consumeMessageLog.setMessageId(message.getMsgID());
                consumeMessageLog.setBornHost(message.getBornHost());
                consumeMessageLog.setBornTimestamp(message.getBornTimestamp());
                consumeMessageLog.setStartDeliverTime(message.getStartDeliverTime());
                consumeMessageLog.setPartition(message.getTopicPartition().getPartition());
                consumeMessageLog.setOffset(message.getOffset());
                consumeMessageLog.setReconsumeTimes(message.getReconsumeTimes());

                consumeMessageLog.setMessageStatus(messageStatus);
                consumeMessageLog.setFailureReason(failureReason);
                consumeMessageLog.setConsumeHost(getLocalHost());
                consumeMessageLog.setConsumer(consumer);
                consumeMessageLog.setConsumeTimestamp(System.currentTimeMillis());
                consumeMessageLog.setGroupId(groupId);

                boolean isRetry = false;
                if (isConsumeSuccess) {
                    isRetry = onsMessageLogService.removeFailureConsumeMessageLog(consumeMessageLog).getDeletedCount() > 0;
                }
                if (isLogEnabled || isRetry) {
                    onsMessageLogService.upsertConsumeMessageLog(consumeMessageLog);
                }
            }
        }catch (Exception e) {
            LOGGER.error("记录消费消息日志失败：", e);
        }
    }

    /**
     * 获取本地主机
     * @return
     */
    @SneakyThrows
    protected static String getLocalHost(){
        return InetAddress.getLocalHost().getHostName();
    }

}
