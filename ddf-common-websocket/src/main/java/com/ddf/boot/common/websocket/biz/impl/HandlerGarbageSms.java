package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.websocket.biz.HandlerTemplateType;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.ParseContent;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 处理垃圾短信
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Service
@Slf4j
public class HandlerGarbageSms implements HandlerTemplateType {

    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;

    /**
     * 当前处理类对应的模板类型,可以多个模板类型对应要给处理类
     *
     * @return
     */
    @Override
    public List<PlatformMessageTemplate.Type> getType() {
        return Collections.singletonList(PlatformMessageTemplate.Type.GARBAGE_SMS);
    }

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
            , MerchantBaseDevice baseDevice, MerchantMessageInfo merchantMessageInfo, Message message) {
        log.error("垃圾短信! [{}]", merchantMessageInfo);
        merchantMessageInfo.setSourceType(MerchantMessageInfo.SOURCE_TYPE_GARBAGE_SMS);
        merchantMessageInfo.setErrorMessage(parseContent.getParseContent());
        merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_SUCCESS);
        merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
    }
}
