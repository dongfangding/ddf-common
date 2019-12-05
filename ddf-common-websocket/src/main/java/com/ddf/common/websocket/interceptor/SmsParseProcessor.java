package com.ddf.common.websocket.interceptor;


import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.ParseContent;

/**
 * 模板解析前置后置处理器
 *
 * @author dongfang.ding
 * @date 2019/9/26 21:14
 */
public interface SmsParseProcessor {

    /**
     * 短信解析前处理器
     *
     * @param authPrincipal
     * @param message
     * @param messageInfo
     * @return
     * @author dongfang.ding
     * @date 2019/9/26 21:16
     */
    void before(AuthPrincipal authPrincipal, Message message, MerchantMessageInfo messageInfo);


    /**
     * 短信解析后处理器
     *
     * @param authPrincipal
     * @param parseContent
     * @param message
     * @param baseDevice
     * @param merchantMessageInfo
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 13:27
     */
    void after(AuthPrincipal authPrincipal, ParseContent parseContent, Message message, MerchantBaseDevice baseDevice
            , MerchantMessageInfo merchantMessageInfo);

}
