package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.websocket.biz.HandlerTemplateType;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.ws.VerifyCodePayload;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import com.ddf.boot.common.util.JsonUtil;
import com.ddf.boot.common.util.StringUtil;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.ParseContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理云闪付验证码
 *
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Service
@Slf4j
public class HandlerUPayVerifyCode implements HandlerTemplateType {

    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;
    @Autowired
    private ChannelTransferService channelTransferService;

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
    public void handler(AuthPrincipal authPrincipal, ParseContent parseContent, MerchantBaseDevice baseDevice, MerchantMessageInfo merchantMessageInfo) {
        try {
            merchantMessageInfo.setSourceType(MerchantMessageInfo.SOURCE_TYPE_UNION_PAY_LOGIN_VERIFY_CODE);
            VerifyCodePayload verifyCodePayload = new VerifyCodePayload();
            verifyCodePayload.setType(VerifyCodePayload.Type.union_pay_login);
            verifyCodePayload.setVerifyCode(parseContent.getVerifyCode());
            Message<VerifyCodePayload> request = Message.request(CmdEnum.VERIFY_CODE, verifyCodePayload);
            String messageStr = JsonUtil.asString(request);
            // 先发送，如果落库失败，至少客户端能收到数据
            WebsocketSessionStorage.sendMessage(authPrincipal, request);
            channelTransferService.recordRequest(authPrincipal, messageStr, request, null);
            merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_SUCCESS);
        } catch (Exception e) {
            log.error("处理发送验证码异常！", e);
            merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_LOGIC_ERROR);
            merchantMessageInfo.setErrorMessage(e.getMessage());
            merchantMessageInfo.setErrorStack(StringUtil.exceptionToString(e));
        }
        merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
    }
}
