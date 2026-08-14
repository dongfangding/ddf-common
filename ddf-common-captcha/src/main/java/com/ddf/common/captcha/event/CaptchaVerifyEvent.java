package com.ddf.common.captcha.event;

import org.springframework.context.ApplicationEvent;

public class CaptchaVerifyEvent extends ApplicationEvent {

    private final String uuid;
    private final boolean success;

    public CaptchaVerifyEvent(Object source, String uuid, boolean success) {
        super(source);
        this.uuid = uuid;
        this.success = success;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isSuccess() {
        return success;
    }
}
