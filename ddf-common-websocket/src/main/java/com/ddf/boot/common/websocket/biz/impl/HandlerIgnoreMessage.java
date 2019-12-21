package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.websocket.biz.HandlerTemplateType;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.ParseContent;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理忽略模板消息
 *
 */
@Service
@Slf4j
public class HandlerIgnoreMessage implements HandlerTemplateType {

    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;

    /**
     * 处理业务
     *
     * @param authPrincipal
     * @param parseContent
     * @param baseDevice
     * @param merchantMessageInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handler(AuthPrincipal authPrincipal, ParseContent parseContent
            , MerchantBaseDevice baseDevice, MerchantMessageInfo merchantMessageInfo) {
        log.error("忽略消息! [{}]", merchantMessageInfo);
        merchantMessageInfo.setSourceType(MerchantMessageInfo.SOURCE_TYPE_IGNORE_MESSAGE);
        merchantMessageInfo.setErrorMessage(parseContent.getParseContent());
        merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_SUCCESS);
        merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
    }
}
