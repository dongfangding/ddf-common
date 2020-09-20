package com.ddf.boot.common.mq.persistence;

import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.mq.entity.LogMqListener;
import com.ddf.boot.common.mq.listener.ListenerQueueEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * mq监听日志落库到Mongo中$
 *
 * @author dongfang.ding
 * @date 2020/9/20 0020 13:30
 */
@Component("mongoLogMqPersistenceProcessor")
@Slf4j
public class MongoLogMqPersistenceProcessor implements LogMqPersistenceProcessor {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 落库方案
     *
     * @param poll          原始数据内容
     * @param logMqListener 处理后的内容
     */
    @Override
    public void persistence(ListenerQueueEntity<?> poll, LogMqListener logMqListener) {
        Query query = new Query();
        String messageId = poll.getMessageWrapper().getMessageId();
        query.addCriteria(Criteria.where("messageId").is(messageId));
        LogMqListener exist = mongoTemplate.findOne(query, LogMqListener.class);
        if (exist == null) {
            // 由于公用了基类，且类型为Long, 这里只能自己生成mongo的objectId
            logMqListener.setId(IdsUtil.getNextLongId());
            mongoTemplate.insert(logMqListener);
        } else {
            if (logMqListener.getEventTimestamp() < exist.getEventTimestamp()) {
                log.error("当前数据小于库中发生时间，不予更新！ {}===>{}", logMqListener, exist);
                return;
            }
            logMqListener.setId(exist.getId());
            logMqListener.setCreateTime(exist.getCreateTime());
            logMqListener.setCreateBy(exist.getCreateBy());
            mongoTemplate.save(logMqListener);
        }
    }
}
