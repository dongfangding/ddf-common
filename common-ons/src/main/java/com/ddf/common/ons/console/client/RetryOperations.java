package com.ddf.common.ons.console.client;

import com.ddf.common.ons.console.constant.RetryChannelEnum;
import com.ddf.common.ons.console.model.ConsoleOnsMessagePushRequest;
import com.ddf.common.ons.console.model.LogRetryRequest;
import com.ddf.common.ons.mongodb.CollectionName;
import com.ddf.common.ons.mongodb.MessageStatusEnum;
import com.ddf.common.ons.mongodb.OnsConsumeMessageLog;
import com.ddf.common.ons.mongodb.OnsConsumeMessageLogQueryVO;
import com.ddf.common.ons.mongodb.OnsMessageLogIdQueryVO;
import com.ddf.common.ons.mongodb.OnsMessageLogRepository;
import com.ddf.common.ons.mongodb.OnsProduceMessageLog;
import com.ddf.common.ons.mongodb.OnsProduceMessageLogQueryVO;
import com.ddf.common.ons.producer.OnsMessage;
import com.ddf.common.ons.producer.OnsProducer;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 16:54
 */
@Component
@Slf4j
public class RetryOperations {

    @Resource(name = "onsProduceMessageLogRepository")
    private OnsMessageLogRepository<OnsProduceMessageLog, OnsProduceMessageLogQueryVO> onsProduceMessageLogRepository;
    @Resource(name = "onsConsumeMessageLogRepository")
    private OnsMessageLogRepository<OnsConsumeMessageLog, OnsConsumeMessageLogQueryVO> onsConsumeMessageLogRepository;

    /**
     * 重试发送消息
     *
     * @param request
     */
    public void retryProduce(LogRetryRequest request) {
        final List<String> list = request.getObjectIdList();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        OnsMessageLogIdQueryVO vo;
        for (String objectId : list) {
            vo = new OnsMessageLogIdQueryVO();
            vo.setObjectId(objectId);
            vo.setCollectionName(new CollectionName(MessageStatusEnum.FAILURE));
            final OnsProduceMessageLog messageLog = onsProduceMessageLogRepository.findByObjectId(vo);
            if (Objects.isNull(messageLog)) {
                log.warn("未找到需要重试的消息记录, objectId: {}", objectId);
                continue;
            }
            final OnsMessage message = messageLog.toOnsMessage();
            try {
                if (StringUtils.isNotBlank(message.getShadingKey())) {
                    OnsProducer.orderSend(message);
                } else {
                    OnsProducer.send(message);
                }
            } catch (Exception ignore) {}
        }
    }

    /**
     * 重试消费消息
     *
     * @param request
     */
    public void retryConsumer(LogRetryRequest request) {
        final List<String> list = request.getObjectIdList();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        OnsMessageLogIdQueryVO vo;
        RetryChannelEnum retryChannel = Objects.isNull(request.getRetryChannel()) ? RetryChannelEnum.ONS_MESSAGE_PUSH :
                request.getRetryChannel();
        for (String objectId : list) {
            try {
                if (Objects.equals(RetryChannelEnum.ONS_DLQ_MESSAGE_RESEND_BY_ID, retryChannel)) {
                    request.checkRequired();
                    OnsClientOperations.onsDLQMessageResendByIdRequest(request.getGroupId(), objectId);
                } else {
                    vo = new OnsMessageLogIdQueryVO();
                    vo.setObjectId(objectId);
                    vo.setCollectionName(new CollectionName(MessageStatusEnum.FAILURE));
                    final OnsConsumeMessageLog messageLog = onsConsumeMessageLogRepository.findByObjectId(vo);
                    if (Objects.isNull(messageLog)) {
                        log.info("未找到需要重试的消息记录, objectId: {}", objectId);
                        continue;
                    }
                    OnsClientOperations.onsMessagePush(new ConsoleOnsMessagePushRequest()
                            .setGroupId(messageLog.getGroupId())
                            .setMsgId(messageLog.getMessageId())
                            .setTopic(messageLog.getTopic()));
                }
            } catch (Exception e) {
                if (list.size() == 1) {
                    throw e;
                }
                // 日志在api层面已经打印，这里主要是不影响其它重试就行了，没有别的处理
            }
        }
    }
}
