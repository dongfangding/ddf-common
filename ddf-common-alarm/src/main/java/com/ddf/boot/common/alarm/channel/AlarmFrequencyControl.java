package com.ddf.boot.common.alarm.channel;

/**
 * 告警频率控制策略，防止告警风暴。接入方可注册自定义实现替换默认 Redis 实现。
 */
public interface AlarmFrequencyControl {

    /**
     * 是否允许发送本次告警（在静默窗口内返回 false）
     */
    boolean tryAcquire(String alarmKey);
}
