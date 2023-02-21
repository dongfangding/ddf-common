package com.ddf.common.ons.consumer;

import com.aliyun.openservices.ons.api.Message;
import com.ddf.common.ons.enume.MessageModel;
import com.ddf.common.ons.mongodb.OnsMessageLogger;
import com.ddf.common.ons.properties.OnsProperties;
import com.ddf.common.ons.redis.OnsRedisService;
import com.ddf.common.ons.redis.RedisKeyConstants;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ONS抽象类
 *
 * @author snowball
 * @date 2021/8/26 16:43
 **/
public abstract class AbstractOnsMessageListener<D extends Serializable> implements OnsMessageListener<D> {

    @Autowired
    protected OnsProperties onsConfiguration;
    @Autowired
    protected OnsRedisService onsRedisService;

    protected Class<D> domainClass;

    @PostConstruct
    protected void init() {
        domainClass = parseDomainClass();
    }

    @Override
    public Class<D> getDomainClass() {
        return domainClass;
    }


    @Override
    public long getIdempotentExpireSeconds() {
        return onsConfiguration.getConsumer()
                .getIdempotentExpireSeconds();
    }

    @Override
    public boolean isBizSuccess(Message message, D domain) {
        return false;
    }

    /**
     * 是否开启消费幂等
     *
     * @return
     * @see AbstractOnsMessageListener#isIdempotentEnabled()
     */
    @Deprecated
    protected boolean isConsumeIdempotentEnsureEnabled() {
        return false;
    }

    /**
     * 是否开启消费幂等
     * 默认：开启
     * 如果消费者重写改方法并关闭了消费幂等，就需要自己去实现isBizSuccess方法保证幂等
     *
     * @return
     * @see OnsMessageListener#isBizSuccess(Message, Serializable)
     */
    protected boolean isIdempotentEnabled() {
        return true;
    }

    /**
     * 确保幂等性
     *
     * @param message
     * @param domain
     * @return
     */
    protected boolean ensureIdempotent(Message message, D domain) {
        // 广播模式暂不支持
        if (MessageModel.BROADCASTING.isMatch(getAnnotation().messageModel()
                .getModel())) {
            return false;
        }
        if (isIdempotentEnabled()) {
            // todo 记录消费模式，将一些广播模式不支持的提特性明确提醒出来
            String key = RedisKeyConstants.getEnsureIdempotentKey(getAnnotation().groupId(), message.getKey());
            final boolean present = !onsRedisService.setIfAbsent(key, message.getMsgID(), getIdempotentExpireSeconds());
            if (present) {
                LOGGER.info("类:{}MessageId:{},Key:{}生成系统判定幂等规则: {}是否已经存在: {}", getMember(), message.getMsgID(),
                        message.getKey(), key, present
                );
            }
            return present;
        } else {
            return isBizSuccess(message, domain);
        }
    }

    /**
     * 重置幂等性偏移量
     *
     * @param message
     */
    protected void resetIdempotentOffset(Message message) {
        if (isIdempotentEnabled()) {
            // 消费失败需要删除才能保证幂等
            onsRedisService.delete(
                    RedisKeyConstants.getEnsureIdempotentKey(getAnnotation().groupId(), message.getKey()));
        }
    }

    /**
     * 记录消费成功消息日志
     *
     * @param message
     */
    protected void infoMessage(Message message) {
        OnsMessageLogger.infoConsumeMessage(
                message, this.getClass()
                        .getCanonicalName(), getAnnotation().groupId());
    }

    /**
     * 记录消费失败消息日志
     *
     * @param message
     */
    protected void errorMessage(Message message, String failureReason) {
        OnsMessageLogger.errorConsumeMessage(
                message, failureReason, this.getClass()
                        .getCanonicalName(), getAnnotation().groupId());
    }

}
