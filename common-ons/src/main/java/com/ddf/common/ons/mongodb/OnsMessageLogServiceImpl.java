package com.ddf.common.ons.mongodb;

import com.ddf.boot.common.core.model.PageResult;
import com.ddf.boot.common.core.util.BeanCopierUtils;
import com.ddf.common.ons.producer.OnsMessage;
import com.ddf.common.ons.producer.OnsProducer;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ONS消息日志应用服务实现类
 *
 * @author snowball
 * @date 2021/8/26 15:29
 **/
@Service(value = OnsMessageLogService.BEAN_NAME)
@Slf4j
public class OnsMessageLogServiceImpl implements OnsMessageLogService {

    @Resource(name = "onsProduceMessageLogRepository")
    private OnsMessageLogRepository<OnsProduceMessageLog, OnsProduceMessageLogQueryVO> onsProduceMessageLogRepository;
    @Resource(name = "onsConsumeMessageLogRepository")
    private OnsMessageLogRepository<OnsConsumeMessageLog, OnsConsumeMessageLogQueryVO> onsConsumeMessageLogRepository;

    @Override
    public void saveProduceMessageLog(OnsProduceMessageLog produceMessageLog) {
        onsProduceMessageLogRepository.save(produceMessageLog);
    }


    /**
     * 保存或更新发送消息日志
     *
     * @param entity
     */
    @Override
    public void upsertProduceMessageLog(OnsProduceMessageLog entity) {
        onsProduceMessageLogRepository.upsert(entity);
    }

    /**
     * 删除发送消息日志
     *
     * @param produceMessageLog
     */
    @Override
    public DeleteResult removeProduceFailureMessageLog(OnsProduceMessageLog produceMessageLog) {
        return onsProduceMessageLogRepository.removeByUniqueQuery(produceMessageLog, Boolean.TRUE);
    }

    @Override
    public PageResult<OnsProduceMessageLog> findProduceMessageLogWithPage(OnsProduceMessageLogQueryVO messageLogQueryVO) {
        return onsProduceMessageLogRepository.findWithPage(messageLogQueryVO);
    }

    @Override
    public void reSendProduceMessage(OnsMessageLogQueryVO messageLogQueryVO) {
        OnsProduceMessageLog produceMessageLog = onsProduceMessageLogRepository.findByMessageId(messageLogQueryVO);
        OnsMessage onsMessage = BeanCopierUtils.copy(produceMessageLog, OnsMessage.class);
        OnsProducer.send(onsMessage);
        // 重发成功之后删除
        onsProduceMessageLogRepository.deleteByMessageId(messageLogQueryVO);
    }

    @Override
    public UpdateResult saveConsumeMessageLog(OnsConsumeMessageLog consumeMessageLog) {
        return onsConsumeMessageLogRepository.upsert(consumeMessageLog);
    }

    /**
     * 保存或更新消费消息日志
     *
     * @param consumeMessageLog
     */
    @Override
    public void upsertConsumeMessageLog(OnsConsumeMessageLog consumeMessageLog) {
        onsConsumeMessageLogRepository.upsert(consumeMessageLog);
    }

    /**
     * 删除发送消息日志
     *
     * @param consumeMessageLog
     */
    @Override
    public DeleteResult removeFailureConsumeMessageLog(OnsConsumeMessageLog consumeMessageLog) {
        return onsConsumeMessageLogRepository.removeByUniqueQuery(consumeMessageLog, Boolean.TRUE);
    }

    @Override
    public PageResult<OnsConsumeMessageLog> findConsumeMessageLogWithPage(OnsConsumeMessageLogQueryVO messageLogQueryVO) {
        return onsConsumeMessageLogRepository.findWithPage(messageLogQueryVO);
    }

    @Override
    public void reSendConsumeMessage(OnsMessageLogQueryVO messageLogQueryVO) {
        OnsConsumeMessageLog consumeMessageLog = onsConsumeMessageLogRepository.findByMessageId(messageLogQueryVO);
        OnsMessage onsMessage = BeanCopierUtils.copy(consumeMessageLog, OnsMessage.class);
        OnsProducer.send(onsMessage);
        // 重发成功之后删除
        onsConsumeMessageLogRepository.deleteByMessageId(messageLogQueryVO);
    }

    @Override
    public void drop() {
        // 删除七天前的生产消息日志
        long epochMilli = LocalDateTime.now().minusDays(7).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        onsProduceMessageLogRepository.drop(new CollectionName(MessageStatusEnum.SUCCESS));
        onsProduceMessageLogRepository.drop(new CollectionName(MessageStatusEnum.FAILURE));
        // 删除七天前的消费消息日志
        onsConsumeMessageLogRepository.drop(new CollectionName(MessageStatusEnum.SUCCESS));
        onsConsumeMessageLogRepository.drop(new CollectionName(MessageStatusEnum.FAILURE));
    }

}
