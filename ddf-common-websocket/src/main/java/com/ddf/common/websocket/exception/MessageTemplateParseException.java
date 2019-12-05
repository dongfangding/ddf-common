package com.ddf.common.websocket.exception;

import com.ddf.common.exception.GlobalCustomizeException;

/**
 * 消息模板数据解析异常
 *
 * @author dongfang.ding
 * @date 2019/9/21 16:11
 */
public class MessageTemplateParseException extends GlobalCustomizeException {


    public MessageTemplateParseException(String message) {
        super(message);
    }

}
