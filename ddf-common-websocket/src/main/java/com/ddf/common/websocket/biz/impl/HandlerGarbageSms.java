package com.ddf.common.websocket.biz.impl;

import com.ddf.common.websocket.biz.HandlerTemplateType;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.ParseContent;
import com.ddf.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理垃圾短信
 *
 */
@Service
@Slf4j
public class HandlerGarbageSms implements HandlerTemplateType {

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
        log.error("垃圾短信! [{}]", merchantMessageInfo);
        merchantMessageInfo.setSourceType(MerchantMessageInfo.SOURCE_TYPE_GARBAGE_SMS);
        merchantMessageInfo.setErrorMessage(parseContent.getParseContent());
        merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_SUCCESS);
        merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
    }
}
