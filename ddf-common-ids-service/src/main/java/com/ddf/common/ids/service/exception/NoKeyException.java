package com.ddf.common.ids.service.exception;

public class NoKeyException extends RuntimeException {
    public NoKeyException(){}
    public NoKeyException(String msg){
        super(msg);
    }
}
