package com.ddf.boot.common.websocket.exception;

import com.ddf.boot.common.core.exception.GlobalCustomizeException;

/**
 * 消息格式无效异常
 *
 * @author dongfang.ding
 * @date 2019/9/20 10:27
 */
public class MessageFormatInvalid extends GlobalCustomizeException {


    public MessageFormatInvalid(String message) {
        super(message);
    }

}
