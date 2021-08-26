package com.ddf.common.ons.mongodb;

import com.ddf.boot.common.core.model.PageResult;
import com.ddf.boot.common.core.util.PageUtil;
import com.ddf.boot.mongo.helper.MongoTemplateHelper;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * ONS消费消息日志仓储实现
 *
 * @author snowball
 * @date 2021/8/26 15:27
 **/
@Repository(value = "onsConsumeMessageLogRepository")
public class OnsConsumeMessageLogRepositoryImpl
        implements OnsMessageLogRepository<OnsConsumeMessageLog, OnsConsumeMessageLogQueryVO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnsConsumeMessageLogRepositoryImpl.class);

    @Resource
    private MongoOperations mongoOperations;
    @Autowired
    private MongoTemplateHelper mongoTemplateHelper;

    @Override
    public void save(OnsConsumeMessageLog entity) {
        mongoOperations.insert(entity, entity.getCollectionName());
    }

    /**
     * 根据业务规则，确定更新或插入一条数据
     *
     * @param entity
     */
    @Override
    public UpdateResult upsert(OnsConsumeMessageLog entity) {
        return mongoOperations.upsert(entity.toUniqueQuery(), entity.toUpdate(), entity.getCollectionName());
    }

    /**
     * 根据对象ID查询消息日志
     *
     * @param query
     * @return
     */
    @Override
    public OnsConsumeMessageLog findByObjectId(OnsMessageLogIdQueryVO query) {
        final String collectionName = query.getCollectionName()
                .getFullName(OnsConsumeMessageLog.class.getSimpleName());
        return mongoOperations.findById(query.getObjectId(), OnsConsumeMessageLog.class, collectionName);
    }

    /**
     * 根据业务唯一规则获取记录
     *
     * @param entity
     * @param inFailureCollection
     * @return
     */
    @Override
    public OnsConsumeMessageLog findByUniqueQuery(OnsConsumeMessageLog entity, boolean inFailureCollection) {
        if (inFailureCollection) {
            return mongoOperations.findOne(
                    entity.toUniqueQuery(), OnsConsumeMessageLog.class, entity.getFailureCollectionName());
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
    public DeleteResult removeByUniqueQuery(OnsConsumeMessageLog entity, boolean inFailureCollection) {
        if (inFailureCollection) {
            return mongoOperations.remove(entity.toUniqueQuery(), entity.getFailureCollectionName());
        }
        return mongoOperations.remove(entity.toUniqueQuery(), entity.getCollectionName());
    }

    @Override
    public PageResult<OnsConsumeMessageLog> findWithPage(OnsConsumeMessageLogQueryVO messageLogQueryVO) {
        LOGGER.info("findWithPage.params:{}", messageLogQueryVO.toString());
        Sort sort = Sort.by(Sort.Direction.DESC, "consumeTimestamp");
        Pageable pageable = PageRequest.of(messageLogQueryVO.getPageNum() - 1, messageLogQueryVO.getPageSize(), sort);

        Query query = new Query();
        if (StringUtils.isNotEmpty(messageLogQueryVO.getTopic())) {
            query.addCriteria(Criteria.where("topic")
                    .is(messageLogQueryVO.getTopic()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getExpression())) {
            Pattern pattern = Pattern.compile(
                    "^.*" + messageLogQueryVO.getExpression() + ".*$", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("expression")
                    .regex(pattern));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getBizId())) {
            query.addCriteria(Criteria.where("bizId")
                    .is(messageLogQueryVO.getBizId()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getShadingKey())) {
            query.addCriteria(Criteria.where("shadingKey")
                    .is(messageLogQueryVO.getShadingKey()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getMessageId())) {
            query.addCriteria(Criteria.where("messageId")
                    .is(messageLogQueryVO.getMessageId()));
        }
        if (Objects.nonNull(messageLogQueryVO.getMessageStartTime()) && Objects.nonNull(
                messageLogQueryVO.getMessageEndTime())) {
            query.addCriteria(Criteria.where("consumeTimestamp")
                    .gte(messageLogQueryVO.getMessageStartTime())
                    .lte(messageLogQueryVO.getMessageEndTime()));
        } else if (Objects.nonNull(messageLogQueryVO.getMessageStartTime())) {
            query.addCriteria(Criteria.where("consumeTimestamp")
                    .gte(messageLogQueryVO.getMessageStartTime()));
        } else if (Objects.nonNull(messageLogQueryVO.getMessageEndTime())) {
            query.addCriteria(Criteria.where("consumeTimestamp")
                    .lte(messageLogQueryVO.getMessageEndTime()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getConsumeHost())) {
            query.addCriteria(Criteria.where("consumeHost")
                    .is(messageLogQueryVO.getConsumeHost()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getConsumer())) {
            query.addCriteria(Criteria.where("consumer")
                    .is(messageLogQueryVO.getConsumer()));
        }
        if (StringUtils.isNotEmpty(messageLogQueryVO.getGroupId())) {
            query.addCriteria(Criteria.where("groupId")
                    .is(messageLogQueryVO.getGroupId()));
        }

        String collectionName = messageLogQueryVO.getCollectionName()
                .getFullName(OnsConsumeMessageLog.class.getSimpleName());
        // 计算总数
        long total = mongoOperations.count(query, OnsConsumeMessageLog.class, collectionName);
        // 查询结果集
        List<OnsConsumeMessageLog> dataList = mongoOperations.find(
                query.with(pageable), OnsConsumeMessageLog.class, collectionName);
        Page<OnsConsumeMessageLog> pageResult = new PageImpl(dataList, pageable, total);
        return PageUtil.convertFromSpringData(pageResult);
    }

    @Override
    public OnsConsumeMessageLog findByMessageId(OnsMessageLogQueryVO messageLogQueryVO) {
        LOGGER.info("findByMessageId.params:{}", messageLogQueryVO.toString());
        Query query = new Query();
        query.addCriteria(Criteria.where("messageId")
                .is(messageLogQueryVO.getMessageId()));
        String collectionName = messageLogQueryVO.getCollectionName()
                .getFullName(OnsConsumeMessageLog.class.getSimpleName());
        return mongoOperations.findOne(query, OnsConsumeMessageLog.class, collectionName);
    }

    @Override
    public long deleteByMessageId(OnsMessageLogQueryVO messageLogQueryVO) {
        LOGGER.info("delete.params:{}", messageLogQueryVO.toString());
        Query query = new Query();
        query.addCriteria(Criteria.where("messageId")
                .is(messageLogQueryVO.getMessageId()));
        String collectionName = messageLogQueryVO.getCollectionName()
                .getFullName(OnsConsumeMessageLog.class.getSimpleName());
        DeleteResult deleteResult = mongoOperations.remove(query, collectionName);
        return deleteResult.getDeletedCount();
    }

    @Override
    public void drop(CollectionName collectionName) {
        LOGGER.info("drop.params:{}", collectionName.toString());
        String collectionFullName = collectionName.getFullName(OnsConsumeMessageLog.class.getSimpleName());
        LOGGER.info("drop collectionFullName:{}", collectionFullName);
        mongoOperations.dropCollection(collectionFullName);
    }

}
