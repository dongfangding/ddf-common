package com.ddf.boot.common.alarm.channel;

import com.ddf.boot.common.alarm.config.LarkProperties;
import com.ddf.boot.common.alarm.model.AlarmMessage;
import com.ddf.boot.common.alarm.model.LarkContentRequest;
import com.ddf.boot.common.alarm.model.LarkTag;
import com.ddf.boot.common.alarm.util.LarkUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Lark 告警渠道实现，逐行渲染为富文本卡片并 @all。
 *
 * @author snowball
 * @version 1.0
 */
public class LarkAlarmChannel implements AlarmChannel {

    private final LarkProperties properties;
    private final String applicationName;

    public LarkAlarmChannel(LarkProperties properties, String applicationName) {
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public String getChannelType() {
        return "lark";
    }

    @Override
    public boolean isEnabled() {
        LarkProperties.Properties p = properties.getCodeProperties(applicationName);
        return p != null && p.isEnabled();
    }

    @Override
    public void send(AlarmMessage message) {
        LarkProperties.Properties p = properties.getCodeProperties(applicationName);
        List<List<LarkTag>> content = new ArrayList<>();
        for (String line : message.getLines()) {
            content.add(List.of(LarkTag.buildText(line)));
        }
        content.add(List.of(LarkTag.buildAtAll()));
        LarkContentRequest request = new LarkContentRequest();
        request.setContent(content);
        LarkUtil.sendPostMsgType(p.getWebhookUrl(), p.getSecret(), message.getTitle(), request);
    }
}
