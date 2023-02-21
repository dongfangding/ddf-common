package com.ddf.common.ons.mongodb;

import com.ddf.common.ons.producer.OnsMessage;
import com.google.common.base.Objects;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * ONS发送消息日志
 *
 * @author snowball
 * @date 2021/8/26 15:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnsProduceMessageLog implements Serializable {

    private static final long serialVersionUID = -5815948957094637400L;

    @Id
    private String id;

    /**
     * 主题
     */
    private String topic;

    /**
     * 路由表达式
     */
    private String expression;

    /**
     * 消息体
     */
    private String payLoad;

    /**
     * 业务Id，每次发送必须唯一
     */
    private String bizId;

    /**
     * 顺序消息必传
     * 分区顺序消息中区分不同分区的关键字段，Sharding Key 与普通消息的 key 是完全不同的概念。
     * 全局顺序消息，该字段可以设置为任意非空字符串。
     */
    private String shadingKey;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 产生消息的主机
     */
    private String bornHost;

    /**
     * 消息的产生时间
     */
    private long bornTimestamp;

    /**
     * 定时消息开始投递时间
     */
    private long startDeliverTime;

    /**
     * 消息所属的 Partition
     */
    private String partition;

    /**
     * 消息在所属 Partition 里的偏移量
     */
    private long offset;

    /**
     * 消息发送/消费状态
     */
    private MessageStatusEnum messageStatus;

    /**
     * 消息发送/消费失败的原因
     */
    private String failureReason;

    public OnsMessage toOnsMessage() {
        final OnsMessage message = new OnsMessage();
        message.setTopic(getTopic());
        message.setExpression(getExpression());
        message.setPayLoad(getPayLoad());
        message.setBizId(getBizId());
        message.setShadingKey(getShadingKey());
        message.setDelayTime(getStartDeliverTime());
        return message;
    }

    /**
     * 获取查询唯一一条记录的查询对象， 当前未记录消息模式，因此不支持广播
     *
     * @return
     */
    public Query toUniqueQuery() {
        Query query = new Query();
        query.addCriteria(Criteria.where("topic").is(getTopic()));
        query.addCriteria(Criteria.where("expression").is(getExpression()));
        query.addCriteria(Criteria.where("bizId").is(getBizId()));
        return query;
    }

    /**
     * 转换为更新字段
     *
     * @return
     */
    public Update toUpdate() {
        Update update = new Update();
        update.set("topic", getTopic());
        update.set("expression", getExpression());
        update.set("payLoad", getPayLoad());
        update.set("bizId", getBizId());
        update.set("shadingKey", getShadingKey());
        update.set("messageId", getMessageId());
        update.set("bornHost", getBornHost());
        update.set("bornTimestamp", getBornTimestamp());
        update.set("startDeliverTime", getStartDeliverTime());
        update.set("partition", getPartition());
        update.set("offset", getOffset());
        update.set("messageStatus", getMessageStatus());
        update.set("failureReason", getFailureReason());
        return update;
    }

    /**
     * 根据消息状态获取最终集合名称
     *
     * @return
     */
    public String getCollectionName() {
        CollectionName collectionName = new CollectionName(getMessageStatus());
        return collectionName.getFullName(this.getClass().getSimpleName());
    }

    /**
     * 根据消息状态获取最终集合名称
     *
     * @return
     */
    public String getFailureCollectionName() {
        CollectionName collectionName = new CollectionName(MessageStatusEnum.FAILURE);
        return collectionName.getFullName(this.getClass().getSimpleName());
    }

    /**
     * 是否要删除发送失败日志
     *
     * @return
     */
    public boolean shouldDeleteFailureLog() {
        return Objects.equal(MessageStatusEnum.SUCCESS, getMessageStatus());
    }
}
