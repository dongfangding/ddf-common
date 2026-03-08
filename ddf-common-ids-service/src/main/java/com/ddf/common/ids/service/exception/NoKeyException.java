package com.ddf.common.ids.service.exception;

public class NoKeyException extends RuntimeException {
    public NoKeyException(){}
    /**
     * @param msg 参数
     */
    public NoKeyException(String msg){
        super(msg);
    }
}