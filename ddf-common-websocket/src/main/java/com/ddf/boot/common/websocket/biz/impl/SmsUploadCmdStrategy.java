package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.websocket.biz.CmdStrategy;
import com.ddf.boot.common.websocket.exception.MessageFormatInvalid;
import com.ddf.boot.common.websocket.helper.CmdStrategyHelper;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.MessageWrapper;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

/**
 * 响应客户端推送的银行收款短信
 *

 * @date 2019/9/7 10:30
 */
@Slf4j
@Service("SMS_UPLOAD")
public class SmsUploadCmdStrategy implements CmdStrategy {
    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;
    @Autowired
    private CmdStrategyHelper cmdStrategyHelper;



    /**
     * 响应Cmd命令码
     *
     * @param webSocketSessionWrapper
     * @param authPrincipal
     * @param message
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal, Message message) {
        log.info("====================响应客户端推送的短信============================");
        log.info("短信响应数据: {}", message);
        if (authPrincipal == null || message == null) {
            throw new MessageFormatInvalid("系统异常，报文数据丢失！！");
        }
        // 获取报文主体数据
        List<Map<String, Object>> payload = CmdStrategyHelper.getListPayload(message, "短信响应报文格式有误！");
        // 记录报文、校验报文、返回有效报文
        MessageWrapper<?, List<MerchantMessageInfo>> messageWrapper = merchantMessageInfoService
                .insertMessageInfoByBankSms(message, payload, authPrincipal);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                // 异步根据保存的有效报文去匹配业务
                cmdStrategyHelper.doSmsUploadBusiness(authPrincipal, message, messageWrapper.getBusinessData());
            }
        });
        // 返回对报文格式的检查结果
        return messageWrapper.getMessage();
    }


}
