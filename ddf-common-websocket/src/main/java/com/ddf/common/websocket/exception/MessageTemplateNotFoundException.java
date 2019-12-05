package com.ddf.common.websocket.exception;

import com.ddf.common.exception.GlobalCustomizeException;

/**
 * 未找到解析消息模板异常
 *
 * @author dongfang.ding
 * @date 2019/9/21 16:10
 */
public class MessageTemplateNotFoundException extends GlobalCustomizeException {


    public MessageTemplateNotFoundException(String message) {
        super(message);
    }

}
