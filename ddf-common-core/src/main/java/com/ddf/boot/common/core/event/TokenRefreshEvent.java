package com.ddf.boot.common.core.event;

import org.springframework.context.ApplicationEvent;

public class TokenRefreshEvent extends ApplicationEvent {

    private final String userId;

    public TokenRefreshEvent(Object source, String userId) {
        super(source);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
