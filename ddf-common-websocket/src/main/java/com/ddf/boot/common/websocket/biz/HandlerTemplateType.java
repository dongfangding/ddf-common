package com.ddf.boot.common.websocket.biz;


import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.ParseContent;

import java.util.List;

/**
 * 根据模板类型处理业务
 *
 * @author dongfang.ding
 * @date 2019/9/27 9:47
 */
public interface HandlerTemplateType {

    /**
     * 当前处理类对应的模板类型,可以多个模板类型对应一个处理类
     * @return
     */
    List<PlatformMessageTemplate.Type> getType();

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
            , MerchantMessageInfo merchantMessageInfo, Message<?> message);
}
