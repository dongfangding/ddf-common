package com.ddf.boot.common.websocket.exception;

import com.ddf.boot.common.exception.GlobalCustomizeException;

/**
 * 无效的未来时间异常
 *
 * @author dongfang.ding
 * @date 2019/10/24 16:02
 */
public class InvalidFutureTimeException extends GlobalCustomizeException {

    public InvalidFutureTimeException(String message) {
        super(message);
    }
}
