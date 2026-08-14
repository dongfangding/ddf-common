package com.ddf.boot.common.alarm.channel;

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
     * 发送告警（标题 + 内容），各渠道自行渲染
     */
    void send(String title, String content);
}
