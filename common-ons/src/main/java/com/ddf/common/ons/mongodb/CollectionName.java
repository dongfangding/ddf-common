package com.ddf.common.ons.mongodb;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * 合集名称
 *
 * @author snowball
 * @date 2021/8/26 15:23
 **/
@Data
public class CollectionName {

    public CollectionName(MessageStatusEnum messageStatus) {
        Assert.notNull(messageStatus, "消息状态不能为空");
        this.messageStatus = messageStatus;
    }

    /**
     * 消息发送/消费状态
     */
    private MessageStatusEnum messageStatus;

    /**
     * 获取集合名字
     * @return
     */
    public String getFullName(String entitySimpleClassName) {
        Assert.hasText(entitySimpleClassName,"实体简单类名不能为空");
        return entitySimpleClassName + getMessageStatus().getCollectionNameSuffix();
    }

    /**
     * 获取集合名字
     * @return
     */
    public String getFailureFullName(String entitySimpleClassName) {
        Assert.hasText(entitySimpleClassName,"实体简单类名不能为空");
        return entitySimpleClassName + getMessageStatus().getCollectionNameSuffix();
    }

}
