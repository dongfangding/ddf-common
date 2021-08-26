package com.ddf.common.ons.consumer.batch;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.common.ons.consumer.AbstractOnsMessageListener;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ONS批量消息监听器抽象类
 *
 * @author snowball
 * @date 2021/8/26 16:45
 **/
public abstract class AbstractBatchOnsMessageListener<D extends Serializable> extends AbstractOnsMessageListener<D>
        implements BatchMessageListener {

    @Override
    public Action consume(final List<Message> messages, final ConsumeContext consumeContext) {
        Set<Action> actions = messages.stream()
                .map(message -> {
                    String payLoad = new String(message.getBody());
                    String messageId = message.getMsgID();
                    String member = getMember();
                    String topic = message.getTopic();
                    try {
                        D domain = JsonUtil.toBean(payLoad, getDomainClass());
                        if (ensureIdempotent(message, domain)) {
                            LOGGER.info("类 [{}] 重复消费 MessageId [{}] Key [{}] 消息", member, messageId, message.getKey());
                            return Action.CommitMessage;
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("类 [{}] 开始消费 MessageId [{}] Key [{}] 消息", member, messageId, message.getKey());
                        }
                        // 调用子类的执行业务方法
                        executeBiz(domain);

                        LOGGER.info("类 [{}] 消费完毕 MessageId [{}] Topic [{}] Tag [{}] PayLoad [{}] Key [{}] 消息", member,
                                messageId, topic, message.getTag(), payLoad, message.getKey()
                        );
                        infoMessage(message);
                        return Action.CommitMessage;
                    } catch (Exception e) {
                        ERROR_LOGGER.error(
                                "类 [{}] 消费失败 MessageId [{}] Topic [{}] Tag [{}] PayLoad [{}] Key [{}] 消息, 原因:", member,
                                messageId, topic, message.getTag(), payLoad, message.getKey(), e
                        );
                        errorMessage(message, e.getMessage());
                        resetIdempotentOffset(message);
                        return Action.ReconsumeLater;
                    }
                })
                .collect(Collectors.toSet());
        // 只要有任何一个消费失败的就返回消费失败，有消费幂等性保证之前消费成功的不会重复消费
        if (actions.stream()
                .anyMatch(action -> Action.ReconsumeLater.name().equals(action.name()))) {
            return Action.ReconsumeLater;
        }
        return Action.CommitMessage;
    }

}
