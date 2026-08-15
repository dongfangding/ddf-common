package com.ddf.boot.common.alarm.channel;

import com.ddf.boot.common.alarm.model.AlarmMessage;

/**
 * 告警渠道策略接口，接入方注册自定义渠道 Bean 即可被聚合分发。
 *
 * @author snowball
 * @version 1.0
 */
public interface AlarmChannel {

    /**
     * 渠道类型标识，如 "dingtalk" / "lark"
     */
    String getChannelType();

    /**
     * 该渠道是否启用
     */
    boolean isEnabled();

    /**
     * 发送结构化告警消息，各渠道按自身能力渲染（钉钉渲染为 markdown，Lark 渲染为富文本卡片）
     */
    void send(AlarmMessage message);
}
