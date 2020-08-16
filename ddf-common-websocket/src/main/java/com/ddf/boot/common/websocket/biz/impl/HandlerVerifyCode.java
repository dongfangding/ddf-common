package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.StringUtil;
import com.ddf.boot.common.websocket.biz.HandlerTemplateType;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.boot.common.websocket.model.payload.VerifyCodePayload;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.ClientChannel;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.ParseContent;
import com.ddf.boot.common.websocket.service.ChannelTransferService;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 处理云闪付验证码
 *
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Service
@Slf4j
public class HandlerVerifyCode implements HandlerTemplateType {

    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;
    @Autowired
    private ChannelTransferService channelTransferService;

    /**
     * 当前处理类对应的模板类型,可以多个模板类型对应要给处理类
     *
     * @return
     */
    @Override
    public List<PlatformMessageTemplate.Type> getType() {
        return Collections.singletonList(PlatformMessageTemplate.Type.PAY_VERIFY_CODE);
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
    public void handler(AuthPrincipal authPrincipal, ParseContent parseContent, MerchantBaseDevice baseDevice
            , MerchantMessageInfo merchantMessageInfo, Message<?> message) {
        log.info("收到[{}]登录验证码短信: {}", message.getClientChannel(), message);
        try {
            merchantMessageInfo.setSourceType(MerchantMessageInfo.SOURCE_TYPE_LOGIN_VERIFY_CODE);
            VerifyCodePayload verifyCodePayload = new VerifyCodePayload();
            verifyCodePayload.setType(VerifyCodePayload.Type.LOGIN);
            verifyCodePayload.setVerifyCode(parseContent.getVerifyCode());
            Message<VerifyCodePayload> request = Message.request(CmdEnum.VERIFY_CODE, verifyCodePayload,
                    ClientChannel.valueOf(parseContent.getPlatformMessageTemplate().getClientChannel()));
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
