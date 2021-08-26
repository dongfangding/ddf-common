package com.ddf.common.ons.mongodb;

import com.ddf.boot.common.core.model.PageResult;
import com.ddf.boot.common.core.util.PageUtil;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * ONS发送消息日志仓储实现
 *
 * @author snowball
 * @date 2021/8/26 15:32
 **/
@Repository(value = "onsProduceMessageLogRepository")
public class OnsProduceMessageLogRepositoryImpl implements OnsMessageLogRepository<OnsProduceMessageLog, OnsProduceMessageLogQueryVO>{

    private static final Logger LOGGER = LoggerFactory.getLogger(OnsProduceMessageLogRepositoryImpl.class);

    @Resource
    private MongoOperations mongoOperations;

    @Override
    public void save(OnsProduceMessageLog entity) {
        CollectionName collectionName = new CollectionName(entity.getMessageStatus());
        mongoOperations.insert(entity, collectionName.getFullName(entity.getClass().getSimpleName()));
    }

    /**
     * 根据业务规则，确定更新或插入一条数据
     *
     * @param entity
     */
    @Override
    public UpdateResult upsert(OnsProduceMessageLog entity) {
        return mongoOperations.upsert(entity.toUniqueQuery(), entity.toUpdate(), entity.getCollectionName());
    }

    /**
     * 根据对象ID查询消息日志
     *
     * @param query
     * @return
     */
    @Override
    public OnsProduceMessageLog findByObjectId(OnsMessageLogIdQueryVO query) {
        final String collectionName = query.getCollectionName()
                .getFullName(OnsProduceMessageLog.class.getSimpleName());
        return mongoOperations.findById(query.getObjectId(), OnsProduceMessageLog.class, collectionName);
    }

    /**
     * 根据业务唯一规则获取记录
     *
     * @param entity
     * @param inFailureCollection
     * @return
     */
    @Override
    public OnsProduceMessageLog findByUniqueQuery(OnsProduceMessageLog entity, boolean inFailureCollection) {
        if (inFailureCollection) {
            return mongoOperations.findOne(entity.toUniqueQuery(), OnsConsumeMessageLog.class, entity.getFailureReason());
        }
        return mongoOperations.findOne(entity.toUniqueQuery(), OnsConsumeMessageLog.class, entity.getCollectionName());
    }

    /**
     * 根据业务唯一规则删除集合数据
     *
     * @param entity
     * @param inFailureCollection 是否在失败的集合中查询记录
     * @return
     */
    @Override
    public DeleteResult removeByUniqueQuery(OnsProduceMessageLog entity, boolean inFailureCollection) {
        if (inFailureCollection) {
            return mongoOperations.remove(entity.toUniqueQuery(), entity.getFailureCollectionName());
        }
        return mongoOperations.remove(entity.toUniqueQuery(), entity.getCollectionName());
    }

    @Override
    public PageResult<OnsProduceMessageLog> findWithPage(OnsProduceMessageLogQueryVO messageLogQueryVO) {
        LOGGER.info("findWithPage.params:{}", messageLogQueryVO.toString());
        Sort sort = Sort.by(Sort.Direction.DESC, "bornTimestamp");
        Pageable pageable = PageRequest.of(messageLogQueryVO.getPageNum() - 1, messageLogQueryVO.getPageSize(), sort);

        Query query = new Query();
        if (StringUtils.isNotEmpty(messageLogQueryVO.getTopic())){
            query.addCriteria(Criteria.where("topic").is(messageLogQueryVO.getTopic()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getExpression())){
            Pattern pattern = Pattern.compile("^.*" + messageLogQueryVO.getExpression() + ".*$", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("expression").regex(pattern));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getBizId())){
            query.addCriteria(Criteria.where("bizId").is(messageLogQueryVO.getBizId()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getShadingKey())){
            query.addCriteria(Criteria.where("shadingKey").is(messageLogQueryVO.getShadingKey()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getMessageId())){
            query.addCriteria(Criteria.where("messageId").is(messageLogQueryVO.getMessageId()));
        }
        if (Objects.nonNull(messageLogQueryVO.getMessageStartTime()) && Objects.nonNull(
                messageLogQueryVO.getMessageEndTime())) {
            query.addCriteria(Criteria.where("bornTimestamp")
                    .gte(messageLogQueryVO.getMessageStartTime())
                    .lte(messageLogQueryVO.getMessageEndTime()));
        } else if (Objects.nonNull(messageLogQueryVO.getMessageStartTime())) {
            query.addCriteria(Criteria.where("bornTimestamp")
                    .gte(messageLogQueryVO.getMessageStartTime()));
        } else if (Objects.nonNull(messageLogQueryVO.getMessageEndTime())) {
            query.addCriteria(Criteria.where("bornTimestamp")
                    .lte(messageLogQueryVO.getMessageEndTime()));
        }

        String collectionName = messageLogQueryVO.getCollectionName().getFullName(OnsProduceMessageLog.class.getSimpleName());
        // 计算总数
        long total = mongoOperations.count(query, OnsProduceMessageLog.class, collectionName);
        // 查询结果集
        List<OnsProduceMessageLog> dataList = mongoOperations.find(query.with(pageable), OnsProduceMessageLog.class, collectionName);
        Page<OnsProduceMessageLog> pageResult = new PageImpl(dataList, pageable, total);
        return PageUtil.convertFromSpringData(pageResult);
    }

    @Override
    public OnsProduceMessageLog findByMessageId(OnsMessageLogQueryVO messageLogQueryVO) {
        LOGGER.info("findByMessageId.params:{}", messageLogQueryVO.toString());
        Query query = new Query();
        query.addCriteria(Criteria.where("messageId").is(messageLogQueryVO.getMessageId()));
        String collectionName = messageLogQueryVO.getCollectionName().getFullName(OnsProduceMessageLog.class.getSimpleName());
        return mongoOperations.findOne(query, OnsProduceMessageLog.class, collectionName);
    }

    @Override
    public long deleteByMessageId(OnsMessageLogQueryVO messageLogQueryVO) {
        LOGGER.info("delete.params:{}", messageLogQueryVO.toString());
        Query query = new Query();
        query.addCriteria(Criteria.where("messageId").is(messageLogQueryVO.getMessageId()));
        String collectionName = messageLogQueryVO.getCollectionName().getFullName(OnsProduceMessageLog.class.getSimpleName());
        DeleteResult deleteResult = mongoOperations.remove(query, collectionName);
        return deleteResult.getDeletedCount();
    }

    @Override
    public void drop(CollectionName collectionName) {
        LOGGER.info("drop.params:{}", collectionName.toString());
        String collectionFullName = collectionName.getFullName(OnsProduceMessageLog.class.getSimpleName());
        LOGGER.info("drop collectionFullName:{}", collectionFullName);
        mongoOperations.dropCollection(collectionFullName);
    }

}
