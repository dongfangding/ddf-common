package com.ddf.common.ons.mongodb;

import com.google.common.base.Objects;
import java.io.Serializable;
import lombok.Data;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * ONS消费消息日志
 *
 * @author snowball
 * @date 2021/8/26 15:24
 **/
@Data
public class OnsConsumeMessageLog extends OnsProduceMessageLog implements Serializable {

    private static final long serialVersionUID = 698802191563391719L;

    /**
     * 消费主机
     */
    private String consumeHost;

    /**
     * 消费者
     * 具体的消费类名
     */
    private String consumer;

    /**
     * 消费时间戳
     */
    private long consumeTimestamp;

    /**
     * GroupID
     */
    private String groupId;

    /**
     * 重试消费次数
     */
    private int reconsumeTimes;

    /**
     * 获取查询唯一一条记录的查询对象， 当前未记录消息模式，因此不支持广播
     *
     * @return
     */
    @Override
    public Query toUniqueQuery() {
        Query query = new Query();
//        query.addCriteria(Criteria.where("topic").is(getTopic()));
//        query.addCriteria(Criteria.where("expression").is(getExpression()));
        query.addCriteria(Criteria.where("groupId").is(getGroupId()));
        query.addCriteria(Criteria.where("bizId").is(getBizId()));
        return query;
    }

    /**
     * 转换为更新字段
     *
     * @return
     */
    @Override
    public Update toUpdate() {
        Update update = new Update();
        update.set("topic", getTopic());
        update.set("expression", getExpression());
        update.set("bizId", getBizId());
        update.set("groupId", getGroupId());
        update.set("messageId", getMessageId());
        update.set("shadingKey", getShadingKey());
        update.set("bornHost", getBornHost());
        update.set("bornTimestamp", getBornTimestamp());
        update.set("startDeliverTime", getStartDeliverTime());
        update.set("partition", getPartition());
        update.set("offset", getOffset());
        update.set("messageStatus", getMessageStatus());
        update.set("failureReason", getFailureReason());
        update.set("consumeHost", getConsumeHost());
        update.set("consumer", getConsumer());
        update.set("consumeTimestamp", getConsumeTimestamp());
        update.set("reconsumeTimes", getReconsumeTimes());
        update.set("payLoad", getPayLoad());
        return update;
    }

    /**
     * 获取当前日志对应的集合名称
     *
     * @return
     */
    @Override
    public String getCollectionName() {
        CollectionName collectionName = new CollectionName(getMessageStatus());
        return collectionName.getFullName(this.getClass().getSimpleName());
    }

    /**
     * 根据消息状态获取最终集合名称
     *
     * @return
     */
    @Override
    public String getFailureCollectionName() {
        CollectionName collectionName = new CollectionName(MessageStatusEnum.FAILURE);
        return collectionName.getFullName(this.getClass().getSimpleName());
    }


    /**
     * 是否要删除消费失败日志
     *
     * @return
     */
    @Override
    public boolean shouldDeleteFailureLog() {
        return Objects.equal(MessageStatusEnum.SUCCESS, getMessageStatus())/* && getReconsumeTimes() > 0*/;
    }
}
