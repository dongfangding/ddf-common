package com.ddf.common.ons.transaction;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 本地事务回查实现
 *
 * @author SteveGuo
 * @date 2021-10-28 17:36 PM
 */
@Component("localTransactionChecker")
public class LocalTransactionCheckerImpl implements LocalTransactionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger("LocalTransactionChecker");

   @Override
   public TransactionStatus check(Message msg) {
       String messageId = msg.getMsgID();
       TransactionStatus transactionStatus = TransactionStatus.RollbackTransaction;
       try {
           String routeKey = msg.getUserProperties(TransactionConst.TRANSACTION_BIZ_ROUTE_KEY);
           if(StringUtils.isEmpty(routeKey)) {
               LOGGER.error("BizId of Message(MessageId:[{}]) is empty", messageId);
           } else {
               boolean isSuccess = BizResultCheckerPool.get(routeKey).isSuccess(msg);
               // 本地事务已成功则提交消息, 本地事务已失败则回滚消息
               transactionStatus = isSuccess ? TransactionStatus.CommitTransaction : TransactionStatus.RollbackTransaction;
           }
       } catch (Exception e) {
           LOGGER.error("Check Biz Result of Message(MessageId:[{}]) occur error", messageId, e);
       }
       LOGGER.info("MessageId:[{}],TransactionStatus:[{}]", messageId, transactionStatus.name());
       return transactionStatus;
   }

 }                        
