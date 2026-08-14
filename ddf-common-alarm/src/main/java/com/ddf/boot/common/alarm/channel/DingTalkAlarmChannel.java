package com.ddf.boot.common.alarm.channel;

import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.util.DingTalkUtil;

/**
 * 钉钉告警渠道实现。
 *
 * @author snowball
 * @version 1.0
 */
public class DingTalkAlarmChannel implements AlarmChannel {

    private final DingTalkProperties properties;
    private final String applicationName;

    public DingTalkAlarmChannel(DingTalkProperties properties, String applicationName) {
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public String getChannelType() {
        return "dingtalk";
    }

    @Override
    public boolean isEnabled() {
        DingTalkProperties.Properties p = properties.getCodeProperties(applicationName);
        return p != null && p.isEnabled();
    }

    @Override
    public void send(String title, String content) {
        DingTalkProperties.Properties p = properties.getCodeProperties(applicationName);
        DingTalkUtil.sendMarkdownMsgToAllWithLimit(p.getSecret(), p.getAccessToken(), title, content);
    }
}
