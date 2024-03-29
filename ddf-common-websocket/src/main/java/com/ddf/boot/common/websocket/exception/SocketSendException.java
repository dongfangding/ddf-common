package com.ddf.boot.common.websocket.exception;

import com.ddf.boot.common.api.exception.ServerErrorException;

/**
 * socket发送数据异常
 *
 * @author dongfang.ding
 * @date 2019/9/21 12:00
 */
public class SocketSendException extends ServerErrorException {

    public SocketSendException(String message) {
        super(message);
    }

    public SocketSendException(Exception e) {
        super(e.getMessage());
    }
}
