package com.ddf.boot.common.core.event;

import com.ddf.boot.common.api.model.authentication.UserClaim;
import org.springframework.context.ApplicationEvent;

public class LoginSuccessEvent extends ApplicationEvent {

    private final UserClaim userClaim;

    public LoginSuccessEvent(Object source, UserClaim userClaim) {
        super(source);
        this.userClaim = userClaim;
    }

    public UserClaim getUserClaim() {
        return userClaim;
    }
}
