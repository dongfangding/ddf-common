package com.ddf.boot.common.alarm.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AlarmMessage} 模型测试。
 *
 * @author snowball
 */
class AlarmMessageTest {

    @Test
    void shouldPopulateViaAllArgsConstructor() {
        AlarmMessage message = new AlarmMessage("告警标题", List.of("line1", "line2"));

        assertThat(message.getTitle()).isEqualTo("告警标题");
        assertThat(message.getLines()).containsExactly("line1", "line2");
    }

    @Test
    void shouldSupportSetterAndNoArgsConstructor() {
        AlarmMessage message = new AlarmMessage();
        message.setTitle("标题");
        message.setLines(List.of("a"));

        assertThat(message.getTitle()).isEqualTo("标题");
        assertThat(message.getLines()).containsExactly("a");
    }
}
