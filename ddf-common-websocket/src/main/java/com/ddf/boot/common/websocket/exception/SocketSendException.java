package com.ddf.boot.common.websocket.exception;

import com.ddf.boot.common.api.exception.ServerErrorException;

/**
 * socket发送数据异常
 *
 * @author dongfang.ding
 * @since 2019/9/21 12:00
 */
public class SocketSendException extends ServerErrorException {
    public SocketSendException(String message) {
        super(message);
    }

    /**
     * @param e 参数
     */
    public SocketSendException(Exception e) {
        super(e.getMessage());
    }
}
