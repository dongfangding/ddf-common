package com.ddf.boot.common.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2024/06/05 19:23
 */
@Getter
public class GlobalExceptionEvent extends ApplicationEvent {

    private final GlobalExceptionEventPayload payload;

    public GlobalExceptionEvent(Object source, GlobalExceptionEventPayload payload) {
        super(source);
        this.payload = payload;
    }
}
