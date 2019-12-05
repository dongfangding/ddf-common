package com.ddf.common.websocket.biz;


import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.ParseContent;

/**
 * 根据模板类型处理业务
 *
 * @author dongfang.ding
 * @date 2019/9/27 9:47
 */
public interface HandlerTemplateType {

    /**
     * 处理业务
     *
     * @param authPrincipal
     * @param parseContent
     * @param baseDevice
     * @param merchantMessageInfo
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 13:28
     */
    void handler(AuthPrincipal authPrincipal, ParseContent parseContent, MerchantBaseDevice baseDevice
            , MerchantMessageInfo merchantMessageInfo);
}
