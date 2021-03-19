package com.ddf.boot.common.core.promise;

/**
 * 回调异常
 *
 * @author dongfang.ding
 * @date 2020/4/9 0009 14:38
 */
public class CallbackTimeoutException extends RuntimeException {

    public CallbackTimeoutException(String requestId) {
        super(requestId);
    }
}
