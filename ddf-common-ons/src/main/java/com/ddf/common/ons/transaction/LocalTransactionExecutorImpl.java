package com.ddf.common.ons.transaction;


import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 本地事务执行器抽象类
 *
 * @author steveguo
 * @date 2021-11-01 3:23 PM
 */
@Component("localTransactionExecutor")
public class LocalTransactionExecutorImpl implements LocalTransactionExecuter {

    protected static final Logger LOGGER = LoggerFactory.getLogger("LocalTransactionExecutor");

    @Override
    public TransactionStatus execute(Message msg, Object arg) {
        String messageId = msg.getMsgID();
        TransactionStatus transactionStatus;
        try {
            String routeKey = msg.getUserProperties(TransactionConst.TRANSACTION_BIZ_ROUTE_KEY);
            if(StringUtils.isEmpty(routeKey) || Objects.isNull(BizTransactionExecutorPool.get(routeKey))) {
                LOGGER.error("没有找到对应消息的事务处理器, routeKey = {}, msg = {}", routeKey, msg);
            }
            boolean isSuccess = BizTransactionExecutorPool.get(routeKey).execute(msg, arg);
            // 本地事务已成功则提交消息, 本地事务已失败则回滚消息
            transactionStatus = isSuccess ? TransactionStatus.CommitTransaction : TransactionStatus.RollbackTransaction;
        } catch (Exception e) {
            LOGGER.error("Execute Biz Transaction of Message(MessageId:[{}]) occur error", messageId, e);
            transactionStatus = TransactionStatus.RollbackTransaction;
        }
        LOGGER.info("MessageId:[{}],TransactionStatus:[{}]", messageId, transactionStatus.name());
        return transactionStatus;
    }

}
