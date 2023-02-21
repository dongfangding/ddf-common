package com.ddf.common.ons.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.ddf.boot.common.api.util.JsonUtil;
import java.io.Serializable;

/**
 * ONS普通无序消息监听器抽象类
 *
 * @author snowball
 * @date 2021/8/26 16:46
 **/
public abstract class AbstractOrdinaryOnsMessageListener<D extends Serializable>
        extends AbstractOnsMessageListener<D> implements MessageListener {

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
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
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("类 [{}] 开始消费 MessageId [{}] Key [{}] 消息", member, messageId, message.getKey());
            }
            // 调用子类的执行业务方法
            executeBiz(domain);

            LOGGER.info("类 [{}] 消费完毕 MessageId [{}] Topic [{}] Tag [{}] PayLoad [{}] Key [{}] 消息", member, messageId, topic,
                    message.getTag(), payLoad, message.getKey());
            infoMessage(message);
            return Action.CommitMessage;
        } catch (Exception e) {
            ERROR_LOGGER.error("类 [{}] 消费失败 MessageId [{}] Topic [{}] Tag [{}] PayLoad [{}] Key [{}] 消息, 原因:",
                    member, messageId, topic, message.getTag(), payLoad, message.getKey(), e);
            errorMessage(message, e.getMessage());
            resetIdempotentOffset(message);
            return Action.ReconsumeLater;
        }
    }

}
