package com.ddf.boot.common.websocket.exception;

import com.ddf.boot.common.exception.GlobalCustomizeException;

/**
 * 客户端重复请求异常
 *
 * @author dongfang.ding
 * @date 2019/9/20 16:11
 */
public class ClientRepeatRequestException extends GlobalCustomizeException {

    public ClientRepeatRequestException(String message) {
        super(message);
    }
}
