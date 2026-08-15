package com.ddf.boot.common.limit.ratelimit.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RateLimitTriggeredEvent} 构造与取值测试。
 *
 * @author snowball
 */
class RateLimitTriggeredEventTest {

    @Test
    void shouldExposeSourceKeyAndAlgorithm() {
        Object source = new Object();

        RateLimitTriggeredEvent event = new RateLimitTriggeredEvent(source, "order:123", "tokenBucket");

        assertThat(event.getSource()).isSameAs(source);
        assertThat(event.getKey()).isEqualTo("order:123");
        assertThat(event.getAlgorithm()).isEqualTo("tokenBucket");
    }
}
