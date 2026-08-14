package com.ddf.boot.common.core.event;

import org.springframework.context.ApplicationEvent;

public class LoginFailureEvent extends ApplicationEvent {

    private final String token;
    private final String errorCode;

    public LoginFailureEvent(Object source, String token, String errorCode) {
        super(source);
        this.token = token;
        this.errorCode = errorCode;
    }

    public String getToken() {
        return token;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
