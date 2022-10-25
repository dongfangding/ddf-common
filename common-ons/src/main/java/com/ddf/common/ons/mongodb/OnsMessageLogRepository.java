package com.ddf.common.ons.mongodb;

import com.ddf.boot.common.api.model.PageResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;


/**
 * ONS消息日志仓储
 *
 * @author snowball
 * @date 2021/8/26 15:29
 **/
public interface OnsMessageLogRepository<E, P> {

    /**
     * 异步保存消息日志
     * @param entity
     */
    void save(E entity);

    /**
     * 根据业务规则，确定更新或插入一条数据
     * @param entity
     */
    UpdateResult upsert(E entity);

    /**
     * 根据对象ID查询消息日志
     * @param query
     * @return
     */
    E findByObjectId(OnsMessageLogIdQueryVO query);

    /**
     * 根据业务唯一规则获取记录
     *
     * @param entity
     * @param inFailureCollection 是否在失败的集合中查询记录
     * @return
     */
    E findByUniqueQuery(E entity, boolean inFailureCollection);

    /**
     * 根据业务唯一规则获取记录
     *
     * @param entity
     * @return
     */
    default E findByUniqueQuery(E entity) {
        return findByUniqueQuery(entity, Boolean.FALSE);
    }

    /**
     * 根据业务唯一规则删除集合数据
     *
     * @param entity
     * @param inFailureCollection 是否在失败的集合中查询记录
     * @return
     */
    DeleteResult removeByUniqueQuery(E entity, boolean inFailureCollection);

    /**
     * 根据业务唯一规则删除集合数据
     *
     * @param entity
     * @return
     */
    default DeleteResult removeByUniqueQuery(E entity) {
        return removeByUniqueQuery(entity, Boolean.FALSE);
    }

    /**
     * 分页查询消息日志
     * @param messageLogQueryVO
     * @return
     */
    PageResult<E> findWithPage(P messageLogQueryVO);

    /**
     * 根据消息ID查询消息日志
     * @param messageLogQueryVO
     * @return
     */
    E findByMessageId(OnsMessageLogQueryVO messageLogQueryVO);

    /**
     * 根据消息ID删除消息日志
     * @param messageLogQueryVO
     * @return
     */
    long deleteByMessageId(OnsMessageLogQueryVO messageLogQueryVO);

    /**
     * 删除集合
     * @param collectionName
     */
    void drop(CollectionName collectionName);

}
