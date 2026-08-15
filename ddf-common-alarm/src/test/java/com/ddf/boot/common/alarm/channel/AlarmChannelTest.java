package com.ddf.boot.common.alarm.channel;

import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.config.LarkProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DingTalkAlarmChannel} / {@link LarkAlarmChannel} 渠道契约测试（渠道类型 + 启用开关）。
 * <p>send 渲染逻辑依赖静态工具类 {@code DingTalkUtil}/{@code LarkUtil}，其 {@code <clinit>} 需要
 * Spring 上下文（{@code SpringContextHolder}），无法在纯单元测试中 mockStatic，故暂不覆盖。</p>
 *
 * @author snowball
 */
class AlarmChannelTest {

    @Test
    void dingTalkShouldExposeChannelType() {
        DingTalkAlarmChannel channel = new DingTalkAlarmChannel(new DingTalkProperties(), "app");

        assertThat(channel.getChannelType()).isEqualTo("dingtalk");
    }

    @Test
    void dingTalkShouldBeEnabledWhenConfigured() {
        DingTalkProperties.Properties p = new DingTalkProperties.Properties();
        p.setEnabled(true);

        DingTalkProperties properties = new DingTalkProperties();
        properties.setCodeException(p);

        DingTalkAlarmChannel channel = new DingTalkAlarmChannel(properties, "app");

        assertThat(channel.isEnabled()).isTrue();
    }

    @Test
    void dingTalkShouldBeDisabledWhenNoProperties() {
        DingTalkAlarmChannel channel = new DingTalkAlarmChannel(new DingTalkProperties(), "app");

        assertThat(channel.isEnabled()).isFalse();
    }

    @Test
    void larkShouldExposeChannelType() {
        LarkAlarmChannel channel = new LarkAlarmChannel(new LarkProperties(), "app");

        assertThat(channel.getChannelType()).isEqualTo("lark");
    }

    @Test
    void larkShouldBeEnabledWhenConfigured() {
        LarkProperties.Properties p = new LarkProperties.Properties();
        p.setEnabled(true);

        LarkProperties properties = new LarkProperties();
        properties.setCodeException(p);

        LarkAlarmChannel channel = new LarkAlarmChannel(properties, "app");

        assertThat(channel.isEnabled()).isTrue();
    }

    @Test
    void larkShouldBeDisabledWhenNoProperties() {
        LarkAlarmChannel channel = new LarkAlarmChannel(new LarkProperties(), "app");

        assertThat(channel.isEnabled()).isFalse();
    }
}
