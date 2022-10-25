package com.ddf.common.ons.mongodb;

import com.ddf.boot.common.api.model.PageResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * ONS消息日志应用服务
 *
 * @author snowball
 * @date 2021/8/26 15:29
 **/
public interface OnsMessageLogService {

    String BEAN_NAME = "onsMessageLogService";

    /**
     * 保存发送消息日志
     * @param produceMessageLog
     */
    void saveProduceMessageLog(OnsProduceMessageLog produceMessageLog);

    /**
     * 保存或更新发送消息日志
     * @param produceMessageLog
     */
    void upsertProduceMessageLog(OnsProduceMessageLog produceMessageLog);

    /**
     * 删除发送消息日志
     * @param produceMessageLog
     */
    DeleteResult removeProduceFailureMessageLog(OnsProduceMessageLog produceMessageLog);

    /**
     * 查找发送消息日志
     * @param messageLogQueryVO
     * @return
     */
    PageResult<OnsProduceMessageLog> findProduceMessageLogWithPage(OnsProduceMessageLogQueryVO messageLogQueryVO);

    /**
     * 重发生产消息
     * @param messageLogQueryVO
     */
    void reSendProduceMessage(OnsMessageLogQueryVO messageLogQueryVO);

    /**
     * 保存消费消息日志
     * @param consumeMessageLog
     */
    UpdateResult saveConsumeMessageLog(OnsConsumeMessageLog consumeMessageLog);

    /**
     * 保存或更新消费消息日志
     * @param consumeMessageLog
     */
    void upsertConsumeMessageLog(OnsConsumeMessageLog consumeMessageLog);

    /**
     * 删除发送消息日志
     * @param consumeMessageLog
     */
    DeleteResult removeFailureConsumeMessageLog(OnsConsumeMessageLog consumeMessageLog);

    /**
     * 查找消费消息日志
     * @param messageLogQueryVO
     * @return
     */
    PageResult<OnsConsumeMessageLog> findConsumeMessageLogWithPage(OnsConsumeMessageLogQueryVO messageLogQueryVO);

    /**
     * 重发消费消息
     * @param messageLogQueryVO
     */
    void reSendConsumeMessage(OnsMessageLogQueryVO messageLogQueryVO);

    /**
     * 删除七天前的生产消息和消费消息日志
     */
    void drop();

}
