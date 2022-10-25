package com.ddf.common.ons.consumer;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;

/**
 * 消费异常
 *
 * @author snowball
 * @date 2021/8/26 16:29
 **/
public class ConsumeException extends BaseException {

    private String code;

    private String message;

    public ConsumeException(String message) {
        super(message);
    }

    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.SERVER_ERROR;
    }

    public ConsumeException(String code, String message) {
        super(code, message);
        this.code = code;
        this.message = message;
    }
}
