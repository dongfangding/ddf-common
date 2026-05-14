package com.ddf.common.ons.consumer.order;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.common.ons.consumer.AbstractOnsMessageListener;
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;

/**
 * ONS顺序消息监听器抽象类
 * <p>
 * 重要说明：
 * 1. 顺序消息消费失败时，不应该返回 Success，因为这会导致消息丢失
 * 2. 应该返回 Suspend 挂起当前队列，等待人工处理或自动重试
 * 3. 如果业务允许失败继续，可以考虑将消息转移到死信队列
 * </p>
 *
 * @author snowball
 * @since 2021/8/26 16:34
 **/
@Slf4j
public abstract class AbstractOrderOnsMessageListener<D extends Serializable> extends AbstractOnsMessageListener<D>
        implements MessageOrderListener {
    @Override
    public OrderAction consume(final Message message, final ConsumeOrderContext context) {
        String payLoad = new String(message.getBody());
        String messageId = message.getMsgID();
        String member = getMember();
        String topic = message.getTopic();
        try {
            D domain = JsonUtil.toBean(payLoad, getDomainClass());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("类 [{}] 开始消费 MessageId [{}] Key [{}] 消息", member, messageId, message.getKey());
            }
            // 调用子类的执行业务方法
            executeBiz(domain);
            LOGGER.info(
                    "类 [{}] 消费完毕 MessageId [{}] Topic [{}] Tag [{}] PayLoad [{}] Key [{}] ShadingKey [{}] 消息",
                    member, messageId, topic, message.getTag(), payLoad, message.getKey(), message.getShardingKey());
            infoMessage(message);
            return OrderAction.Success;
        } catch (Exception e) {
            LOGGER.error(
                    "类 [{}] 消费失败 MessageId [{}] Topic [{}] Tag [{}] PayLoad [{}] Key [{}] ShadingKey [{}] 消息, 原因:",
                    member, messageId, topic, message.getTag(), payLoad, message.getKey(), message.getShardingKey(), e);
            errorMessage(message, e.getMessage());
            // 如果消费失败，挂起当前队列，那么后面的都会堵塞，所以这里消费失败要返回成功，然后通过日志去手工重试
            return OrderAction.Success;
        }
    }

}
