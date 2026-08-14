package com.ddf.boot.common.limit.ratelimit.event;

import org.springframework.context.ApplicationEvent;

public class RateLimitTriggeredEvent extends ApplicationEvent {

    private final String key;
    private final String algorithm;

    public RateLimitTriggeredEvent(Object source, String key, String algorithm) {
        super(source);
        this.key = key;
        this.algorithm = algorithm;
    }

    public String getKey() {
        return key;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
